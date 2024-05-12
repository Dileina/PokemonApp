package com.example.pokemonapp
import PokeApiService
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokemonapp.databinding.ActivityPokemonListBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class PokemonListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPokemonListBinding
    private lateinit var pokeApiService: PokeApiService
    private lateinit var adapter: PokemonListAdapter
    private var fullPokemonList: MutableList<Pokemon> = mutableListOf()
    private var allPokemonList: MutableList<Pokemon> = mutableListOf() // Maintain a list of all Pokémon

    private var offset = 0
    private val limit = 5
    private var isLoading = false
    private var hasMorePokemons = true

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        binding = ActivityPokemonListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        pokeApiService = retrofit.create(PokeApiService::class.java)

        adapter = PokemonListAdapter { pokemonName ->
            startActivity(
                PokemonDetailsActivity.createIntent(this, pokemonName)
            )
        }

        binding.pokemonRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.pokemonRecyclerView.adapter = adapter
        loadMorePokemon()

        binding.pokemonRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { // Check if scrolling down
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount
                    if (!isLoading && hasMorePokemons && lastVisibleItemPosition >= totalItemCount - 2) {
                        loadMorePokemon()
                    }
                }
            }
        })

        setupSearch()  // Initialize search functionality
    }

    private fun loadMorePokemon() {
        isLoading = true
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = pokeApiService.getPokemonList(limit, offset * limit)
                if (response.results.isEmpty()) {
                    hasMorePokemons = false
                } else {
                    val pokemonsWithTypes = response.results.map { pokemon ->
                        val url = pokemon.url
                        val parts = url.trimEnd('/').split("/")
                        val id = parts.last()
                        val details = pokeApiService.getPokemonDetailsById(id.toInt())
                        pokemon.copy(types = details.types.map { it.type.name.capitalize(Locale.ROOT) })
                    }
                    fullPokemonList.addAll(pokemonsWithTypes)
                    allPokemonList.addAll(pokemonsWithTypes) // Update the all Pokémon list
                    adapter.submitList(fullPokemonList.toList()) // ToList for a new instance
                    offset += 1
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
                setupTypeFilters()
            }
        }
    }

    private fun setupTypeFilters() {
        val buttonContainer = binding.root.findViewById<LinearLayout>(R.id.typeButtonContainer)
        buttonContainer.removeAllViews()

        val allButton = MaterialButton(this).apply {
            text = "All"
            setOnClickListener {
                adapter.submitList(fullPokemonList)
            }
        }
        buttonContainer.addView(allButton)

        val allTypes = fullPokemonList.flatMap { it.types ?: emptyList() }.distinct()
        allTypes.forEach { type ->
            val button = MaterialButton(this).apply {
                text = type
                setOnClickListener {
                    val filteredList = fullPokemonList.filter { pokemon ->
                        pokemon.types?.contains(type) == true
                    }
                    adapter.submitList(filteredList)
                }
            }
            buttonContainer.addView(button)
        }
    }

    private fun setupSearch() {
        val searchView = binding.root.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.lowercase(Locale.getDefault()) ?: ""
                val filteredList = allPokemonList.filter { pokemon ->
                    pokemon.name.contains(query, ignoreCase = true) ||
                            (pokemon.types?.any { it.contains(query, ignoreCase = true) } ?: false)
                }
                adapter.submitList(filteredList)
                return true
            }
        })
    }
}

