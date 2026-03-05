package com.financasdacasa.app.util

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.*

fun getLucideIcon(name: String): ImageVector = when (name) {
    "Wallet" -> Lucide.Wallet
    "BadgeDollarSign" -> Lucide.BadgeDollarSign
    "PiggyBank" -> Lucide.PiggyBank
    "TrendingUp" -> Lucide.TrendingUp
    "TrendingDown" -> Lucide.TrendingDown
    "Receipt" -> Lucide.Receipt
    "Home" -> Lucide.House
    "Utensils" -> Lucide.Utensils
    "Coffee" -> Lucide.Coffee
    "FileText" -> Lucide.FileText
    "Layers" -> Lucide.Layers
    "Car" -> Lucide.Car
    "Bus" -> Lucide.Bus
    "Bike" -> Lucide.Bike
    "Helmet" -> Lucide.HardHat
    "Heart" -> Lucide.Heart
    "Sparkles" -> Lucide.Sparkles
    "GraduationCap" -> Lucide.GraduationCap
    "Shirt" -> Lucide.Shirt
    "Gift" -> Lucide.Gift
    "Wifi" -> Lucide.Wifi
    "ShoppingCart" -> Lucide.ShoppingCart
    "ShoppingBag" -> Lucide.ShoppingBag
    "Banknote" -> Lucide.Banknote
    "CreditCard" -> Lucide.CreditCard
    "Building" -> Lucide.Building
    "Plane" -> Lucide.Plane
    "Music" -> Lucide.Music
    "Tv" -> Lucide.Tv
    "Smartphone" -> Lucide.Smartphone
    "BookOpen" -> Lucide.BookOpen
    "Briefcase" -> Lucide.Briefcase
    "Phone" -> Lucide.Phone
    "Monitor" -> Lucide.Monitor
    "Scissors" -> Lucide.Scissors
    "Wrench" -> Lucide.Wrench
    "Baby" -> Lucide.Baby
    "PawPrint" -> Lucide.PawPrint
    "Dog" -> Lucide.Dog
    "Dumbbell" -> Lucide.Dumbbell
    "Paintbrush" -> Lucide.Paintbrush
    "ShieldCheck" -> Lucide.ShieldCheck
    "Landmark" -> Lucide.Landmark
    "Stethoscope" -> Lucide.Stethoscope
    "Pill" -> Lucide.Pill
    "Flame" -> Lucide.Flame
    "Lightbulb" -> Lucide.Lightbulb
    "Droplets" -> Lucide.Droplets
    "Coins" -> Lucide.Coins
    else -> Lucide.Tag
}
