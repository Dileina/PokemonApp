package com.example.pokemonapp
import PokeApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.pokemonapp.databinding.ActivityPokemonDetailsBinding
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Suppress("DEPRECATION")
class PokemonDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPokemonDetailsBinding
    private lateinit var pokeApiService: PokeApiService

    companion object {
        private const val POKEMON_ID = "POKEMON_ID"

        fun createIntent(context: Context, pokemonId: Int): Intent {
            return Intent(context, PokemonDetailsActivity::class.java).apply {
                putExtra(POKEMON_ID, pokemonId)
            }
        }
    }

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        binding = ActivityPokemonDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.root.findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        pokeApiService = retrofit.create(PokeApiService::class.java)

        var pokemonId = intent.getIntExtra(POKEMON_ID, 1)

        // Load the initial Pokémon details
        loadPokemonDetails(pokemonId)

        binding.previousButton.setOnClickListener {
            pokemonId -= 1
            Log.d("PIDORASKA", pokemonId.toString())
            if (pokemonId > 0) {
                loadPokemonDetails(pokemonId)
            }
        }

        binding.nextButton.setOnClickListener {
            pokemonId += 1
            Log.d("PIDORASKA", pokemonId.toString())
            if (pokemonId > 0) {
                loadPokemonDetails(pokemonId)
            }
        }
    }

    private fun loadPokemonDetails(pokemonId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = pokeApiService.getPokemonDetailsById(pokemonId)
                Glide.with(binding.pokemonImage.context)
                    .load(response.sprites.front_default)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(binding.pokemonImage)

                val hp = response.stats.find { it.stat.name == "hp" }?.base_stat ?: 0
                val attack = response.stats.find { it.stat.name == "attack" }?.base_stat ?: 0
                val defense = response.stats.find { it.stat.name == "defense" }?.base_stat ?: 0

                val heightInMeters = response.height / 10.0
                val weightInKilograms = response.weight / 10.0

                binding.pokemonNameText.text = response.name
                binding.pokemonHeightText.text = "Height: %.1f m".format(heightInMeters)
                binding.pokemonWeightText.text = "Weight: %.1f kg".format(weightInKilograms)
                binding.pokemonTypeText.text = "Type: ".format(response.types)

                binding.pokemonHpText.text = "HP: $hp"
                binding.pokemonAttackText.text = "Attack: $attack"
                binding.pokemonDefenseText.text = "Defense: $defense"
                binding.pokemonTypeText.text = "Type: ${response.types.joinToString(", ") { it.type.name }}"
                binding.pokemonSpecialAttackText.text = "Special Attack: ${response.stats.find { it.stat.name == "special-attack" }?.base_stat ?: 0}"
                binding.pokemonSpecialDefenseText.text = "Special Defense: ${response.stats.find { it.stat.name == "special-defense" }?.base_stat ?: 0}"
                binding.pokemonSpeedText.text = "Speed: ${response.stats.find { it.stat.name == "speed" }?.base_stat ?: 0}"
                intent.putExtra(POKEMON_ID, response.id)
                binding.previousButton.isEnabled = response.id > 1
                binding.nextButton.isEnabled = true

            } catch (e: Exception) {
                Log.e("PokemonDetailsActivity", "Error fetching pokemon details", e)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
