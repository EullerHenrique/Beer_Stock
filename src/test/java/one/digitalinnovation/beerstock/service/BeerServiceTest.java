package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*

    Testes de Unidade ou teste unitário é a fase de testes onde cada unidade do sistema é testada individualmente.
    O objetivo é isolar cada parte do sistema para garantir que elas estejam funcionando conforme especificado.


 */

/*
    Junit

        Esse framework facilita a criação e manutenção do código para a automação de testes com apresentação dos
        resultados.
        Com ele, pode ser verificado se cada método de uma classe funciona da forma esperada, exibindo possíveis
        erros ou falhas podendo ser utilizado tanto para a execução de baterias de testes como para extensão.

        Com JUnit, o programador tem a possibilidade de usar esta ferramenta para criar um modelo padrão de testes,
        muitas vezes de forma automatizada.

        O teste de unidade testa o menor dos componentes de um sistema de maneira isolada. Cada uma dessas unidades
        define um conjunto de estímulos (chamada de métodos), e de dados de entrada e saída associados a cada estímulo.
        As entradas são parâmetros e as saídas são o valor de retorno, exceções ou o estado do objeto. Tipicamente
        um teste unitário executa um método individualmente e compara uma saída conhecida após o processamento da mesma.

*/

/* Mockito

    O Mockito é um framework de testes unitários e o seu principal objetivo é instanciar classes e controlar o
    comportamento dos métodos. Isso é chamado de mock, na tradução livre quer dizer zombar, e talvez seja mesmo o termo
    que melhor o define.
    Pois ao mockar a dependencia de uma classe, eu faço com que a classe que estou testando pense estar invocando o metodo
    realmente, mas de fato não está. Conforme o desenho abaixo tenta explicar.

*/


/* hamcrest

    O Hamcrest é um framework que possibilita a criação de regras de verificação (matchers) de forma declarativa.
    Como dito no próprio site do Hamcrest, Matchers that can be combined to create flexible expressions of intent.

    Portanto a ideia é que com os matchers Hamcrest as asserções utilizadas expressem melhor a sua intenção,
    ficando mais legíveis e mais expressivas.

    Um matcher Hamcrest é um objeto que
        reporta se um dado objeto satisfaz um determinado critério;
        pode descrever este critério; e
        é capaz de descrever porque um objeto não satisfaz um determinado critério.


*/


//MockitoExtension.class = Extensão necessária para as anotações do mockito serem utilizadas
@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    // Mock: cria uma instancia de uma classe, porém Mockada (simulada). Se você chamar um metodo ele não irá chamar
    // o metodo real, a não ser que você queira.
    @Mock
    private BeerRepository beerRepository;

    // BeerMapper

    //  toModel = DTO -> MODEL
    //  toDTO = MODEL -> DTO

    // MODEL = O model é a camada que possui a lógica da aplicação. Ele é o responsável pelas regras de negócios,
    // persistência com o banco de dados e as classes de entidades. O model recebe as requisições vindas do controller
    // e gera respostas a partir destas requisições

    // DTO = Objeto de Transferência de Dados (do inglês, Data transfer object, ou simplesmente DTO), é um padrão
    // de projeto de software usado para transferir dados entre subsistemas de um software.
    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    // InjectMocks: Cria uma intancia e injeta as dependências necessárias que estão anotadas com @Mock.
    @InjectMocks
    private BeerService beerService;

    //When: Após um mock ser criado, você pode direcionar um retorno para um metodo dado um parametro de entrada.

    // @Test = A anotação de teste informa ao JUnit que o método void público ao qual está anexado pode ser executado
    // como um caso de teste . Para executar o método, JUnit primeiro constrói uma nova instância da classe e,
    // em seguida,  invoca o método anotado

    // Quando a cerveja é informada, ela deve ser criada throws Exceção de cerveja já registrada
    @Test
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {

        // Dado

            //Gera um BeerDTO
            BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //Tranforma o BeertDTO em um BeerModel
            Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

        //Quando

            //expectedSavedBeerDTO for salvo ou não -> expectedSavedBeer
            when(beerRepository.save(expectedSavedBeer))
                    .thenReturn(expectedSavedBeer);

        // Então

            //Cria um BeerDTO
            BeerDTO createdBeerDTO = beerService.createBeer(expectedBeerDTO);

            //Verifica se o atributo id do createdBeerDTO é igual ao atributo id do expectedDTO
            assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));

            //Verifica se o atributo name do createdBeerDTO é igual ao atributo name do expectedDTO
            assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));

            //Verifica se o atributo quantitiy do creadtedBeerDTO é igual ao atributo quantity do expectedBeerDTO
            assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));

    }


    // Quando a cerveja já registrada é informada, então uma exceção deve ser lançada
    @Test
    void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrown() {

        // Dado

            //Gera um BeerDTO
            BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //Tranforma o BeertDTO em um BeerModel
            Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);

        // Quando

            //expectedBeerDTO.getName() for encontrado ou não -> Optional.of(duplicatedBeer)
            when(beerRepository.findByName(expectedBeerDTO.getName()))
                    .thenReturn(Optional.of(duplicatedBeer));

        // Então

            //Verifica se beerService.createBeer(expectedBeerDTO) lançou a exceção BeerAlreadyRegisteredException.class
            assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));

    }

    //Quando um nome de cerveja válido é fornecido, então retorna uma cerveja -> Exceção de cerveja não encontrada
    @Test
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {

        // Dado

            //Gera um BeerDTO
            BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //Tranforma o BeertDTO em um BeerModel
            Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        // Quando

            //expectedFoundBeer.getName() for encontrado ou não -> Optional.of(expectedFoundBeer)
            when(beerRepository.findByName(expectedFoundBeer.getName()))
                    .thenReturn(Optional.of(expectedFoundBeer));

        // Então

            //foundBeerDTO recebe a cerveja encontrada
            BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());

            //Verifica se o objeto foundBeerDTO é igual ao objeto expecterFoundBeerDTO
            assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
    }

    //Quando um nome de cerveja não registrado é fornecido, então lança uma exceção
    @Test
    void whenNotRegisteredBeerNameIsGivenThenThrowAnException() {

        // Dado

            //Gera um BeerDTO
            BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // Quando

            //expectedFoundBeerDTO.getName() for encontrado ou não -> Optional.empty()
            when(beerRepository.findByName(expectedFoundBeerDTO.getName()))
                    .thenReturn(Optional.empty());

        // Então

            //Verifica se beerService.findByName(expectedFoundBeerDTO.getName()) lançou a exceção BeerNotFoundException.class
            assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));

    }


    //Quando uma lista de cervejas for chamada, então retorne uma lista de cervejas
    @Test
    void whenListBeerIsCalledThenReturnAListOfBeers() {
        // Dado

            //Gera um BeerDTO
            BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //Tranforma o BeertDTO em um BeerModel
            Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //when

            //beerService.listAll() retornar uma lista ou não -> Collections.singletonList(beerDTO)
            when(beerRepository.findAll())
                    .thenReturn(Collections.singletonList(expectedFoundBeer));

        //then

            //foundBeerDTO recebe a lista de cerveja
            List<BeerDTO> foundListBeersDTO = beerService.listAll();

            //Verifica se foundListBeersDTO não está vazio
            assertThat(foundListBeersDTO, is(not(empty())));

            //Verifica se o primeiro elemento da lista presente no objeto foundListBeersDTO é
            //igual ao elemento presente no objeto expectedFoundBeerDTO
            assertThat(foundListBeersDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
    }

    // Quando a lista de cerveja é chamada, então retorna uma lista vazia de cervejas
    @Test
    void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {

        //Quando

            //beerService.findAll() retornar uma lista ou não -> Collections.singletonList(beerDTO)
            when(beerRepository.findAll())
                    .thenReturn(Collections.EMPTY_LIST);

        //Então

            //foundBeerDTO recebe a lista de cerveja
            List<BeerDTO> foundListBeersDTO = beerService.listAll();

            //Verifica se foundListBeersDTO está vazio
            assertThat(foundListBeersDTO, is(empty()));
    }

    //Quando a exclusão é chamada com ID válido, então uma cerveja deve ser excluída
    @Test
    void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException{

        // Dado

            //Gera um BeerDTO
            BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //Tranforma o BeertDTO em um BeerModel
            Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);

        // Quando

            //beerService.findById() retornar uma lista ou não -> Optional.of(expectedDeletedBeer)
            when(beerRepository.findById(expectedDeletedBeerDTO.getId()))
                    .thenReturn(Optional.of(expectedDeletedBeer));

        //Não faça nada

            //Quando beerRepository.deleteById(expectedDeletedBeerDTO.getId());
            doNothing()
                    .when(beerRepository)
                    .deleteById(expectedDeletedBeerDTO.getId());
            //Obs: doNothing() é utilizado porque deleteByID não retorna nada

        // Então

            //Um beerDTO é excluído pelo seu ID
            beerService.deleteById(expectedDeletedBeerDTO.getId());

            //Verify: Verifica se um metódo de uma classe foi testado (no when) e verifica
            //a quantidade de vezes que ele foi testado (no when).

            //Verifica se o metódo findById da classe beerRepository foi chamado uma vez
            verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());

            //Verifica se o metódo deleteById da classe beerRepository foi chamado uma vez
            verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
    }


    //Quando o incremento é chamado, então aumente o BeerStock
    @Test
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {

        //Dado

            //Gera um BeerDTO
            BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //Tranforma o BeertDTO em um BeerModel
            Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //Quando

            //expectedBeerDTO.getId() for encontrado ou não -> Optional.of(expectedBeer)
            when(beerRepository.findById(expectedBeerDTO.getId()))
                    .thenReturn(Optional.of(expectedBeer));

            //expectedBeer for salvo ou não -> expectedBeer
            when(beerRepository.save(expectedBeer))
                    .thenReturn(expectedBeer);

        int quantityToIncrement = 10;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        // Então

            //Gera um BeerDTO
            BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

            //Verifica se expectedQuantityAfterIncrement é igual a não incrementedBeerDTO.getQuantity() io
            assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));

            //Verifica se expectedQuantityAfterIncrement é menor que expectedBeerDTO.getMax()
            assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));

    }

    //Quando o incremento for maior que o máximo, então lance uma  exceção
    @Test
    void whenIncrementIsGreatherThanMaxThenThrowException() {

        //Dado

            //Gera um BeerDTO
            BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //Tranforma o BeertDTO em um BeerModel
            Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //Quando

            //expectedBeerDTO.getId() for encontrado ou não -> Optional.of(expectedBeer
            when(beerRepository.findById(expectedBeerDTO.getId()))
                    .thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 80;
        //Então

            //Verifica se beerService.increment(expectedBeerDTO.getId(), quantityToIncrement) lançou a exceção
            //BeerStockExceededException.class
            assertThrows(BeerStockExceededException.class,
                    () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    //Quando o incremento após a soma for maior do que o máximo, então lance a exceção
    @Test
    void whenIncrementAfterSumIsGreatherThanMaxThenThrowException() {

        //Dado

            //Gera um BeerDTO
            BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

            //Tranforma o BeertDTO em um BeerModel
            Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //Quando

            //expectedBeerDTO.getId() for encontrado ou não -> Optional.of(expectedBeer
            when(beerRepository.findById(expectedBeerDTO.getId()))
                    .thenReturn(Optional.of(expectedBeer));

            int quantityToIncrement = 45;

            //Verifica se beerService.increment(expectedBeerDTO.getId(), quantityToIncrement) lançou a exceção
            //BeerStockExceededException.class
            assertThrows(BeerStockExceededException.class,
                    () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));

    }

    //Quando o incremento é chamado com ID inválido, então lança exceção
    @Test
    void whenIncrementIsCalledWithInvalidIdThenThrowException() {

        //Dado

            int quantityToIncrement = 10;

        //Quando

            //INVALID_BEER_ID for encontrado ou não -> Optional.of(expectedBeer)
            when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        //Então

            //Verifica se beerService.increment(expectedBeerDTO.getId(), quantityToIncrement) lançou a exceção
            //BeerStockExceededException.class
            assertThrows(BeerNotFoundException.class,
                    () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }

}
