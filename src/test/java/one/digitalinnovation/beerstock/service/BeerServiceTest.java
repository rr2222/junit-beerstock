package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.builder.SecondBeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static one.digitalinnovation.beerstock.enums.BeerType.LAGER;
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

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;

    @Test
    void whenBeerInformedIsGonnaCreateABeer() throws BeerAlreadyRegisteredException {
        // Given
        BeerDTO givenBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer givenBeerEntity = BeerMapper.INSTANCE.toModel(givenBeerDTO);

        when(beerRepository.save(givenBeerEntity)).thenReturn(givenBeerEntity);
        BeerDTO result = beerService.createBeer(givenBeerDTO);
        Assertions.assertEquals(givenBeerDTO, result);
    }

    @Test
    void whenBeerInformedButExistsThrowException(){
        // Given
        BeerDTO givenBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer givenBeerEntity = BeerMapper.INSTANCE.toModel(givenBeerDTO);
        when(beerRepository.findByName(givenBeerDTO.getName())).thenReturn(Optional.of(givenBeerEntity));
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(givenBeerDTO));

    }

    @Test
    void whenBeerInformedFindThatBeerByName() throws BeerNotFoundException {
        // Given
        BeerDTO givenBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer givenBeerEntity = BeerMapper.INSTANCE.toModel(givenBeerDTO);
        BeerDTO secondBeerDTO = SecondBeerDTOBuilder.builder().build().toBeerDTO();

        when(beerRepository.findByName(givenBeerDTO.getName())).thenReturn(Optional.of(givenBeerEntity));

        //then
        BeerDTO foundBeer = beerService.findByName(givenBeerDTO.getName());
        Assertions.assertEquals(foundBeer, secondBeerDTO);
    }

    @Test
    void whenBeerInformedIsNotFoundThrowException(){
        BeerDTO givenBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer givenBeerEntity = BeerMapper.INSTANCE.toModel(givenBeerDTO);

        when(beerRepository.findByName(givenBeerDTO.getName())).thenReturn(Optional.empty());
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(givenBeerDTO.getName()));
    }

    @Test
    void whenCallListFindAllBeers(){
        BeerDTO givenBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer givenBeerEntity = BeerMapper.INSTANCE.toModel(givenBeerDTO);

        when(beerRepository.findAll()).thenReturn(Collections.singletonList(givenBeerEntity));

        List<BeerDTO> foundListOfBeers = beerService.listAll();
        Assertions.assertEquals(foundListOfBeers, Collections.singletonList(givenBeerDTO));
    }

    @Test
    void whenCallDeleteBeerWillDelete() throws BeerNotFoundException {
        BeerDTO givenBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer givenBeerEntity = BeerMapper.INSTANCE.toModel(givenBeerDTO);

        when(beerRepository.findById(givenBeerDTO.getId())).thenReturn(Optional.of(givenBeerEntity));
        doNothing().when(beerRepository).deleteById(givenBeerDTO.getId());
        beerService.deleteById(givenBeerDTO.getId());

        verify(beerRepository, times(1)).findById(givenBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(givenBeerDTO.getId());

    }

    @Test
    void whenCallIncrementBeerWillIncrement() throws BeerNotFoundException, BeerStockExceededException {
        BeerDTO givenBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer givenBeerEntity = BeerMapper.INSTANCE.toModel(givenBeerDTO);

        when(beerRepository.findById(givenBeerDTO.getId())).thenReturn(Optional.of(givenBeerEntity));
        when(beerRepository.save(givenBeerEntity)).thenReturn(givenBeerEntity);
        int quantityToIncrement = 10;
        int quantityAfterIncrement = givenBeerDTO.getQuantity() + quantityToIncrement;

        BeerDTO savedBeer = beerService.increment(givenBeerDTO.getId(), quantityToIncrement);
        Assertions.assertEquals(quantityAfterIncrement, savedBeer.getQuantity());
    }

    @Test
    void whenCallIncrementBeerAndExcedWillThrowException(){
        BeerDTO givenBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer givenBeerEntity = BeerMapper.INSTANCE.toModel(givenBeerDTO);

        when(beerRepository.findById(givenBeerDTO.getId())).thenReturn(Optional.of(givenBeerEntity));
        int quantityToIncrement = 50;


        assertThrows(BeerStockExceededException.class, () -> beerService.increment(givenBeerDTO.getId(), quantityToIncrement));
    }

}
