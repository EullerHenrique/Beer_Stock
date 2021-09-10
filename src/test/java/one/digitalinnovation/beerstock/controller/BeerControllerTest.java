package one.digitalinnovation.beerstock.controller;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.dto.QuantityDTO;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.service.BeerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Collections;

import static one.digitalinnovation.beerstock.utils.JsonConvertionUtils.asJsonString;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

    private static final String BEER_API_URL_PATH = "/api/v1/beers";
    private static final long VALID_BEER_ID = 1L;
    private static final long INVALID_BEER_ID = 2l;
    private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

    //MockMvc: fornece suporte para teste Spring MVC.
    // Ele encapsula todos os beans de aplicativo da we e os disponibiliza para teste.
    private MockMvc mockMvc;

    // Mock: cria uma instancia de uma classe, porém Mockada (simulada). Se você chamar um metodo ele não irá chamar
    // o metodo real, a não ser que você queira.
    @Mock
    private BeerService beerService;

    // InjectMocks: Cria uma intancia e injeta as dependências necessárias que estão anotadas com @Mock.
    @InjectMocks
    private BeerController beerController;

    //When: Após um mock ser criado, você pode direcionar um retorno para um metodo dado um parametro de entrada.

    // @BeforeEach: Execute antes de cada método de teste.
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    // @Test = A anotação de teste informa ao JUnit que o método void público ao qual está anexado pode ser executado
    // como um caso de teste . Para executar o método, JUnit primeiro constrói uma nova instância da classe e,
    // em seguida,  invoca o método anotado

    //Quando o POST é chamado, então uma cerveja é criada
    @Test
    void whenPOSTIsCalledThenABeerIsCreated() throws Exception {

        //Dado

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //Quando

            //beerDTO for criado ou não -> beerDTO
            when(beerService.createBeer(beerDTO))
                    .thenReturn(beerDTO);

        // Então

            //perform: Executa o post /api/v1/beers
            //contentType: Define que o tipo do conteúdo é JSON
            //content: Define que o conteúdo é o Json de beerDTO
            //andExpect: Espera-se que o post retorne o status created
            //andExpect: Espera-se que $.name seja igual a beerDTO.getName()
            //andExpect: Espera-se que $.type seja igual a beerType()
            mockMvc.perform(post(BEER_API_URL_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(beerDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                    .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                    .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
    }

    // Quando o POST é chamado sem campo obrigatório, então um erro é retornado
    @Test
    void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorIsReturned() throws Exception {

        // Dado

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //O atributo Brand do objeto BeerDTO recebe o valor null
            beerDTO.setBrand(null);

        //When

            //beerDTO for inválido
            //A anotação @Valid beerDTO presente no metódo createBeer que realiza tal verificação

        // Então

            //perform: Executa o post /api/v1/beers
            //contentType: Define que o tipo do conteúdo é JSON
            //andExpect: Espera-se que o post retorne o status BadRequest

            mockMvc.perform(post(BEER_API_URL_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(beerDTO)))
                    .andExpect(status().isBadRequest());
    }

    //Quando GET é chamado com nome válido, então o status OK é retornado
    @Test
    void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {

        // Dado

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // Quando

            //beerDTO.getName() for encontrado ou não -> beerDTO
            when(beerService.findByName(beerDTO.getName()))
                    .thenReturn(beerDTO);

        // Então

            //perform: Executa o get /api/v1/beers + beerDTO.getName()
            //contentType: Define que o tipo do conteúdo é JSON
            //andExpect: Espera-se que o get retorne o status OK
            //andExpect: Espera-se que $.name seja igual a beerDTO.getName()
            //andExpect: Espera-se que $.brand seja igual a beerDTO.getBrand()
            //andExpect: Espera-se que $.type seja igual a beerType()

            mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + beerDTO.getName())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                    .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                    .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
    }

    // Quando GET é chamado sem nome registrado, então o status Not Found  é retornado
    @Test
    void whenGETIsCalledWithoutRegisteredNameThenNotFoundStatusIsReturned() throws Exception {

        // Dado

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // Quando

            //beerDTO.getName() for encontrado ou não -> Exceção BeerNotFoundException.class
            when(beerService.findByName(beerDTO.getName()))
                    .thenThrow(BeerNotFoundException.class);

        // Então

            //perform: Executa o get /api/v1/beers + beerDTO.getName()
            //contentType: Define que o tipo do conteúdo é JSON
            //andExpect: Espera-se que o get retorne o status NotFound

            mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + beerDTO.getName())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
    }

    //Quando a lista GET com cervejas é chamada, o status OK é retornado
    @Test
    void whenGETListWithBeersIsCalledThenOkStatusIsReturned() throws Exception {

        // Dado

        //Gera um BeerDTO
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // Quando

        //beerService.listAll() retornar uma lista ou não -> Collections.singletonList(beerDTO)
        when(beerService.listAll())
                .thenReturn(Collections.singletonList(beerDTO));

        // Então

            //perform: Executa o get /api/v1/beers
            //contentType: Define que o tipo do conteúdo é JSON
            //andExpect: Espera-se que o get retorne o status OK
            //andExpect: Espera-se que $[0].name seja igual a beerDTO.getName()
            //andExpect: Espera-se que $[0].brand seja igual a beerDTO.getBrand()
            //andExpect: Espera-se que $[0].type seja igual a beerType()


            mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name", is(beerDTO.getName())))
                    .andExpect(jsonPath("$[0].brand", is(beerDTO.getBrand())))
                    .andExpect(jsonPath("$[0].type", is(beerDTO.getType().toString())));
    }

    //Quando a lista GET sem cervejas é chamada, então o status OK é retornado
    @Test
    void whenGETListWithoutBeersIsCalledThenOkStatusIsReturned() throws Exception {

        // Dado

        //Gera um BeerDTO
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //Quando

        //beerService.listAll() retornar uma lista ou não -> Collections.singletonList(beerDTO)
        when(beerService.listAll())
                .thenReturn(Collections.singletonList(beerDTO));

        // Então

            //perform: Executa o get /api/v1/beers
            //contentType: Define que o tipo do conteúdo é JSON
            //andExpect: Espera-se que o get retorne o status OK

            mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
    }

    //Quando DELETE é chamado com um ID válido, então o status NoContent é retornado
    @Test
    void whenDELETEIsCalledWithValidIdThenNoContentStatusIsReturned() throws Exception {

        // Dado

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //Não faça nada

           //Quando beerRepository.deleteById(expectedDeletedBeerDTO.getId());
            doNothing()
                    .when(beerService)
                    .deleteById(beerDTO.getId());
            //Obs: doNothing() é utilizado porque deleteByID não retorna nada

        // Então

            //perform: Executa o delete /api/v1/beers + beerDTO.getId()
            //contentType: Define que o tipo do conteúdo é JSON
            //andExpect: Espera-se que o delete retorne o status NoContent

            mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + beerDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

    }


    //Quando DELETE é chamado com ID inválido, o status NotFound é retornado
    @Test
    void whenDELETEIsCalledWithInvalidIdThenNotFoundStatusIsReturned() throws Exception {

        //Gere uma exceção
            //Quando beerService.deleteById(INVALID_BEER_ID)
            doThrow(BeerNotFoundException.class).
                    when(beerService).deleteById(INVALID_BEER_ID);

        // Então

            //perform: Executa o delete /api/v1/beers + INVALID_BEER_ID
            //contentType: Define que o tipo do conteúdo é JSON
            //andExpect: Espera-se que o delete retorne o status NotFound
            mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
    }

    //Quando PATCH é chamado para aumentar o desconto, então o status OK é retornado
    @Test
    void whenPATCHIsCalledToIncrementDiscountThenOKstatusIsReturned() throws Exception {

        //Dado

            //Gera um quantityDTO e gera o quantity = 10
            QuantityDTO quantityDTO = QuantityDTO.builder()
                    .quantity(10)
                    .build();

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //Defina o valor do atributo quantity do beerDTO
            beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());

        //Quando

            //beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity() retornar um BeerDTO ou não -> beerDTO
            when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity()))
                    .thenReturn(beerDTO);

        //Então

            //perform: Executa o patch BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL
            //contentType: Define que o tipo do conteúdo é JSON
            //andExpect: Espera-se que o get retorne o status OK
            //andExpect: Espera-se que $name seja igual a beerDTO.getName()
            //andExpect: Espera-se que $brand seja igual a beerDTO.getBrand()
            //andExpect: Espera-se que $type seja igual a beerDTO.getType()
            //andExpect: Espera-se que $quantity seja igual a beerDTO.getQuantity()

            mockMvc.perform(MockMvcRequestBuilders.patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(quantityDTO))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                    .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                    .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
                    .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));

    }

}



