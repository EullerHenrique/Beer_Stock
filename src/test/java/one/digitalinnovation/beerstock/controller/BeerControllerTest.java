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

    // Mock: cria uma instancia de uma classe, por??m Mockada (simulada). Se voc?? chamar um metodo ele n??o ir?? chamar
    // o metodo real, a n??o ser que voc?? queira.
    @Mock
    private BeerService beerService;

    // InjectMocks: Cria uma intancia e injeta as depend??ncias necess??rias que est??o anotadas com @Mock.
    @InjectMocks
    private BeerController beerController;

    //When: Ap??s um mock ser criado, voc?? pode direcionar um retorno para um metodo dado um parametro de entrada.

    // @BeforeEach: Execute antes de cada m??todo de teste.
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    // @Test = A anota????o de teste informa ao JUnit que o m??todo void p??blico ao qual est?? anexado pode ser executado
    // como um caso de teste . Para executar o m??todo, JUnit primeiro constr??i uma nova inst??ncia da classe e,
    // em seguida,  invoca o m??todo anotado

    //Quando o POST ?? chamado, ent??o uma cerveja ?? criada
    @Test
    void whenPOSTIsCalledThenABeerIsCreated() throws Exception {

        //Dado

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //Quando

            //beerDTO for criado ou n??o -> beerDTO
            when(beerService.createBeer(beerDTO))
                    .thenReturn(beerDTO);

        // Ent??o

            //perform: Executa o post /api/v1/beers
            //contentType: Define que o tipo do conte??do ?? JSON
            //content: Define que o conte??do ?? o Json de beerDTO
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

    // Quando o POST ?? chamado sem campo obrigat??rio, ent??o um erro ?? retornado
    @Test
    void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorIsReturned() throws Exception {

        // Dado

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //O atributo Brand do objeto BeerDTO recebe o valor null
            beerDTO.setBrand(null);

        //When

            //beerDTO for inv??lido
            //A anota????o @Valid beerDTO presente no met??do createBeer que realiza tal verifica????o

        // Ent??o

            //perform: Executa o post /api/v1/beers
            //contentType: Define que o tipo do conte??do ?? JSON
            //andExpect: Espera-se que o post retorne o status BadRequest

            mockMvc.perform(post(BEER_API_URL_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(beerDTO)))
                    .andExpect(status().isBadRequest());
    }

    //Quando GET ?? chamado com nome v??lido, ent??o o status OK ?? retornado
    @Test
    void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {

        // Dado

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // Quando

            //beerDTO.getName() for encontrado ou n??o -> beerDTO
            when(beerService.findByName(beerDTO.getName()))
                    .thenReturn(beerDTO);

        // Ent??o

            //perform: Executa o get /api/v1/beers + beerDTO.getName()
            //contentType: Define que o tipo do conte??do ?? JSON
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

    // Quando GET ?? chamado sem nome registrado, ent??o o status Not Found  ?? retornado
    @Test
    void whenGETIsCalledWithoutRegisteredNameThenNotFoundStatusIsReturned() throws Exception {

        // Dado

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // Quando

            //beerDTO.getName() for encontrado ou n??o -> Exce????o BeerNotFoundException.class
            when(beerService.findByName(beerDTO.getName()))
                    .thenThrow(BeerNotFoundException.class);

        // Ent??o

            //perform: Executa o get /api/v1/beers + beerDTO.getName()
            //contentType: Define que o tipo do conte??do ?? JSON
            //andExpect: Espera-se que o get retorne o status NotFound

            mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH + "/" + beerDTO.getName())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
    }

    //Quando a lista GET com cervejas ?? chamada, o status OK ?? retornado
    @Test
    void whenGETListWithBeersIsCalledThenOkStatusIsReturned() throws Exception {

        // Dado

        //Gera um BeerDTO
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // Quando

        //beerService.listAll() retornar uma lista ou n??o -> Collections.singletonList(beerDTO)
        when(beerService.listAll())
                .thenReturn(Collections.singletonList(beerDTO));

        // Ent??o

            //perform: Executa o get /api/v1/beers
            //contentType: Define que o tipo do conte??do ?? JSON
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

    //Quando a lista GET sem cervejas ?? chamada, ent??o o status OK ?? retornado
    @Test
    void whenGETListWithoutBeersIsCalledThenOkStatusIsReturned() throws Exception {

        // Dado

        //Gera um BeerDTO
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //Quando

        //beerService.listAll() retornar uma lista ou n??o -> Collections.singletonList(beerDTO)
        when(beerService.listAll())
                .thenReturn(Collections.singletonList(beerDTO));

        // Ent??o

            //perform: Executa o get /api/v1/beers
            //contentType: Define que o tipo do conte??do ?? JSON
            //andExpect: Espera-se que o get retorne o status OK

            mockMvc.perform(MockMvcRequestBuilders.get(BEER_API_URL_PATH)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
    }

    //Quando DELETE ?? chamado com um ID v??lido, ent??o o status NoContent ?? retornado
    @Test
    void whenDELETEIsCalledWithValidIdThenNoContentStatusIsReturned() throws Exception {

        // Dado

            //Gera um BeerDTO
            BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //N??o fa??a nada

           //Quando beerRepository.deleteById(expectedDeletedBeerDTO.getId());
            doNothing()
                    .when(beerService)
                    .deleteById(beerDTO.getId());
            //Obs: doNothing() ?? utilizado porque deleteByID n??o retorna nada

        // Ent??o

            //perform: Executa o delete /api/v1/beers + beerDTO.getId()
            //contentType: Define que o tipo do conte??do ?? JSON
            //andExpect: Espera-se que o delete retorne o status NoContent

            mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + beerDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

    }


    //Quando DELETE ?? chamado com ID inv??lido, o status NotFound ?? retornado
    @Test
    void whenDELETEIsCalledWithInvalidIdThenNotFoundStatusIsReturned() throws Exception {

        //Gere uma exce????o
            //Quando beerService.deleteById(INVALID_BEER_ID)
            doThrow(BeerNotFoundException.class).
                    when(beerService).deleteById(INVALID_BEER_ID);

        // Ent??o

            //perform: Executa o delete /api/v1/beers + INVALID_BEER_ID
            //contentType: Define que o tipo do conte??do ?? JSON
            //andExpect: Espera-se que o delete retorne o status NotFound
            mockMvc.perform(MockMvcRequestBuilders.delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
    }

    //Quando PATCH ?? chamado para aumentar o desconto, ent??o o status OK ?? retornado
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

            //beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity() retornar um BeerDTO ou n??o -> beerDTO
            when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity()))
                    .thenReturn(beerDTO);

        //Ent??o

            //perform: Executa o patch BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL
            //contentType: Define que o tipo do conte??do ?? JSON
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



