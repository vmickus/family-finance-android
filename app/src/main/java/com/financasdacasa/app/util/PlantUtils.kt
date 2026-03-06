package com.financasdacasa.app.util

import com.financasdacasa.app.R

val GOAL_COLOR_OPTIONS = listOf(
    "#5B8A72", "#6B9EB8", "#7BAFB8", "#8CAA7E",
    "#C4A96B", "#C4836B", "#9B8ABF", "#BF8B8B",
)

const val DEFAULT_GOAL_COLOR = "#5B8A72"

fun getStageCount(plantType: String): Int = when (plantType) {
    "tree" -> 9
    else -> 5
}

fun getPlantStage(progress: Double, maxStages: Int): Int {
    val stage = (progress * maxStages).toInt()
    return stage.coerceIn(0, maxStages - 1)
}

fun getPlantDrawable(plantType: String, progress: Double): Int {
    val maxStages = getStageCount(plantType)
    val stage = getPlantStage(progress, maxStages) + 1 // stages are 1-indexed in filenames
    return when (plantType) {
        "tree" -> when (stage) {
            1 -> R.drawable.plant_tree_stage_1
            2 -> R.drawable.plant_tree_stage_2
            3 -> R.drawable.plant_tree_stage_3
            4 -> R.drawable.plant_tree_stage_4
            5 -> R.drawable.plant_tree_stage_5
            6 -> R.drawable.plant_tree_stage_6
            7 -> R.drawable.plant_tree_stage_7
            8 -> R.drawable.plant_tree_stage_8
            else -> R.drawable.plant_tree_stage_9
        }
        "sunflower" -> when (stage) {
            1 -> R.drawable.plant_sunflower_stage_1
            2 -> R.drawable.plant_sunflower_stage_2
            3 -> R.drawable.plant_sunflower_stage_3
            4 -> R.drawable.plant_sunflower_stage_4
            else -> R.drawable.plant_sunflower_stage_5
        }
        "bonsai" -> when (stage) {
            1 -> R.drawable.plant_bonsai_stage_1
            2 -> R.drawable.plant_bonsai_stage_2
            3 -> R.drawable.plant_bonsai_stage_3
            4 -> R.drawable.plant_bonsai_stage_4
            else -> R.drawable.plant_bonsai_stage_5
        }
        "cactus" -> when (stage) {
            1 -> R.drawable.plant_cactus_stage_1
            2 -> R.drawable.plant_cactus_stage_2
            3 -> R.drawable.plant_cactus_stage_3
            4 -> R.drawable.plant_cactus_stage_4
            else -> R.drawable.plant_cactus_stage_5
        }
        else -> R.drawable.plant_tree_stage_1
    }
}

val PLANT_TYPE_OPTIONS = listOf("tree", "sunflower", "bonsai", "cactus")
