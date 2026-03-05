package com.financasdacasa.app.util

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.*

fun getLucideIcon(name: String): ImageVector = when (name) {
    "Wallet" -> Lucide.Wallet
    "PiggyBank" -> Lucide.PiggyBank
    "TrendingUp" -> Lucide.TrendingUp
    "TrendingDown" -> Lucide.TrendingDown
    "Receipt" -> Lucide.Receipt
    "Home" -> Lucide.House
    "Utensils" -> Lucide.Utensils
    "FileText" -> Lucide.FileText
    "Layers" -> Lucide.Layers
    "Car" -> Lucide.Car
    "Heart" -> Lucide.Heart
    "Sparkles" -> Lucide.Sparkles
    "GraduationCap" -> Lucide.GraduationCap
    "Shirt" -> Lucide.Shirt
    "Gift" -> Lucide.Gift
    "Wifi" -> Lucide.Wifi
    "ShoppingCart" -> Lucide.ShoppingCart
    "Banknote" -> Lucide.Banknote
    "CreditCard" -> Lucide.CreditCard
    "Building" -> Lucide.Building
    "Plane" -> Lucide.Plane
    "Coffee" -> Lucide.Coffee
    "Music" -> Lucide.Music
    "BookOpen" -> Lucide.BookOpen
    "Briefcase" -> Lucide.Briefcase
    "Phone" -> Lucide.Phone
    "Monitor" -> Lucide.Monitor
    "Scissors" -> Lucide.Scissors
    "Wrench" -> Lucide.Wrench
    "Baby" -> Lucide.Baby
    "Dog" -> Lucide.Dog
    "Dumbbell" -> Lucide.Dumbbell
    "Paintbrush" -> Lucide.Paintbrush
    "ShieldCheck" -> Lucide.ShieldCheck
    "Landmark" -> Lucide.Landmark
    "Stethoscope" -> Lucide.Stethoscope
    "Pill" -> Lucide.Pill
    "Flame" -> Lucide.Flame
    else -> Lucide.Tag
}
