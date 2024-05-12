package com.example.pokemonapp
data class Pokemon(
    val name: String,
    val url: String,
    var types: List<String>? = null,
    val next: String?,
    val previous: String?,
    val speed: Int?,
    val specialAttack: Int?,
    val specialDefense: Int?,
    val id: Int
)

data class PokemonListResponse(
    val results: List<Pokemon>
)

data class PokemonDetailsResponse(
    val name: String,
    val height: Double,
    val weight: Double,
    val sprites: Sprites,
    val stats: List<Stat>,
    val types: List<PokemonType>,
    val id: Int
)

data class Stat(
    val base_stat: Int,
    val stat: StatDetail
)

data class StatDetail(
    val name: String
)

data class PokemonType(
    val type: TypeDetail
)

data class TypeDetail(
    val name: String
)

data class Sprites(
    val front_default: String
)
