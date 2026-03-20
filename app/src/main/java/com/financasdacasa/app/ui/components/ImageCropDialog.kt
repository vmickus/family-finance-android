package com.financasdacasa.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.financasdacasa.app.R
import kotlin.math.min
import kotlin.math.roundToInt

private const val MAX_DIMENSION = 1024

private fun decodeSampledBitmap(
    context: android.content.Context,
    uri: Uri,
): Bitmap? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

    val (w, h) = options.outWidth to options.outHeight
    if (w <= 0 || h <= 0) return null

    var sampleSize = 1
    while (w / sampleSize > MAX_DIMENSION || h / sampleSize > MAX_DIMENSION) {
        sampleSize *= 2
    }

    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val raw = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOpts) }
        ?: return null

    // Apply EXIF rotation
    val rotation = context.contentResolver.openInputStream(uri)?.use { stream ->
        val exif = ExifInterface(stream)
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } ?: 0f

    if (rotation == 0f) return raw

    val matrix = Matrix().apply { postRotate(rotation) }
    return Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
}

@Composable
fun ImageCropDialog(
    imageUri: Uri,
    onConfirm: (Bitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val bitmap = remember(imageUri) { decodeSampledBitmap(context, imageUri) }

    if (bitmap == null) {
        onDismiss()
        return
    }

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), color = Color.White)
                    }
                    TextButton(onClick = {
                        val cropped = cropCircular(bitmap, scale, offset)
                        if (cropped != null) onConfirm(cropped) else onDismiss()
                    }) {
                        Text(stringResource(R.string.save), color = Color.White)
                    }
                }

                // Canvas area
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 5f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val canvasW = constraints.maxWidth.toFloat()
                    val canvasH = constraints.maxHeight.toFloat()
                    val circleRadius = min(canvasW, canvasH) * 0.4f

                    // Layer 1: the image
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val bw = bitmap.width.toFloat()
                        val bh = bitmap.height.toFloat()

                        val fitScale = min(canvasW / bw, canvasH / bh)
                        val totalScale = fitScale * scale

                        val imgW = (bw * totalScale).roundToInt()
                        val imgH = (bh * totalScale).roundToInt()
                        val imgX = ((canvasW - imgW) / 2f + offset.x).roundToInt()
                        val imgY = ((canvasH - imgH) / 2f + offset.y).roundToInt()

                        drawImage(
                            image = imageBitmap,
                            dstOffset = IntOffset(imgX, imgY),
                            dstSize = IntSize(imgW, imgH),
                        )
                    }

                    // Layer 2: overlay with circular cutout (using Path, no BlendMode)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(canvasW / 2f, canvasH / 2f)
                        val overlayPath = Path().apply {
                            addRect(Rect(0f, 0f, canvasW, canvasH))
                            addOval(Rect(center.x - circleRadius, center.y - circleRadius, center.x + circleRadius, center.y + circleRadius))
                        }
                        // fillType EvenOdd makes the oval a hole in the rect
                        overlayPath.fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                        drawPath(overlayPath, Color.Black.copy(alpha = 0.6f))

                        // Circle border
                        drawCircle(
                            color = Color.White.copy(alpha = 0.7f),
                            radius = circleRadius,
                            center = center,
                            style = Stroke(width = 2f),
                        )
                    }
                }
            }
        }
    }
}

private fun cropCircular(
    source: Bitmap,
    scale: Float,
    offset: Offset,
): Bitmap? {
    val bw = source.width.toFloat()
    val bh = source.height.toFloat()

    // We need to map the circle back to source bitmap coordinates.
    // The virtual canvas size doesn't matter — use a canonical size.
    val canvasSize = 1000f
    val circleRadius = canvasSize * 0.4f
    val fitScale = min(canvasSize / bw, canvasSize / bh)
    val totalScale = fitScale * scale

    val imgX = (canvasSize - bw * totalScale) / 2f + offset.x
    val imgY = (canvasSize - bh * totalScale) / 2f + offset.y

    val cx = canvasSize / 2f
    val cy = canvasSize / 2f

    // Map circle bounds to source coordinates
    val srcLeft = ((cx - circleRadius - imgX) / totalScale).roundToInt().coerceIn(0, source.width)
    val srcTop = ((cy - circleRadius - imgY) / totalScale).roundToInt().coerceIn(0, source.height)
    val srcRight = ((cx + circleRadius - imgX) / totalScale).roundToInt().coerceIn(0, source.width)
    val srcBottom = ((cy + circleRadius - imgY) / totalScale).roundToInt().coerceIn(0, source.height)

    val cropW = srcRight - srcLeft
    val cropH = srcBottom - srcTop
    if (cropW <= 0 || cropH <= 0) return null

    val cropped = Bitmap.createBitmap(source, srcLeft, srcTop, cropW, cropH)

    // Scale to square output
    val outputSize = min(cropW, cropH)
    return Bitmap.createScaledBitmap(cropped, outputSize, outputSize, true)
}
