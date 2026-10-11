package com.kevin.memoverse.domain.model

/** Le jardin à un instant donné : les plantes et le délai avant le prochain arrosage. */
data class Garden(
    val plants: List<GardenPlant>,
    /** Délai avant qu'un verset ait soif, ou null si un verset a déjà soif (ou jardin vide). */
    val millisUntilNextWatering: Long?,
) {
    val thirstyCount: Int get() = plants.count { it.isThirsty }
}
