import com.example.pokemonapp.PokemonDetailsResponse
import com.example.pokemonapp.PokemonListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(@Query("limit") limit: Int, @Query("offset") offset: Int): PokemonListResponse

    @GET("pokemon/{id}")
    suspend fun getPokemonDetailsById(@Path("id") id: Int): PokemonDetailsResponse
}