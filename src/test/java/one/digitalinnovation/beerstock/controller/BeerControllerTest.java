package one.digitalinnovation.beerstock.controller;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.dto.QuantityDTO;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
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
import java.util.Optional;

import static one.digitalinnovation.beerstock.utils.JsonConvertionUtils.asJsonString;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

    private static final String BEER_API_URL_PATH = "/api/v1/beers";
    private static final long VALID_BEER_ID = 1L;
    private static final long INVALID_BEER_ID = 2l;
    private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

    private MockMvc mockMvc;

    @Mock
    private BeerService beerService;

    @InjectMocks
    private BeerController beerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Test
    void verifyIfControllerPOSTCreateABeer() throws Exception {
        BeerDTO beerDTOToCreate = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerService.createBeer(beerDTOToCreate)).thenReturn(beerDTOToCreate);

        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType("application/json")
                .content(asJsonString(beerDTOToCreate)))
                .andExpect(status().isCreated());

    }

    @Test
    void whenPOSTMethodCreatedRequiredErrorIsReturn() throws Exception {
        BeerDTO beerDTOToCreate = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTOToCreate.setBrand(null);

        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType("application/json")
                .content(asJsonString(beerDTOToCreate))).andExpect(status().isBadRequest());
    }

    @Test
    void whenGETMethodFindByNameIsCallReturnOk() throws Exception {
        BeerDTO beerDTOToCreate = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerService.findByName(beerDTOToCreate.getName())).thenReturn(beerDTOToCreate);

        mockMvc.perform(get(BEER_API_URL_PATH + "/" + beerDTOToCreate.getName())
                .contentType("application/json")).andExpect(status().isOk());
    }

    @Test
    void whenGETMethodFindByNameIsCallAndBeerNotFoundReturnNOTFOUND() throws Exception {
        BeerDTO beerDTOToCreate = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerService.findByName(beerDTOToCreate.getName())).thenThrow(BeerNotFoundException.class);


        mockMvc.perform(get(BEER_API_URL_PATH + "/" + beerDTOToCreate.getName())
                .contentType("application/json")).andExpect(status().isNotFound());

    }

    @Test
    void methodGETReturnListAndStatusOK() throws Exception {
        BeerDTO beerDTOToCreate = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerService.listAll()).thenReturn(Collections.singletonList(beerDTOToCreate));

        mockMvc.perform(get(BEER_API_URL_PATH).contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void methodGETReturnEmptyListAndStatus() throws Exception {
        BeerDTO beerDTOToCreate = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerService.listAll()).thenReturn(Collections.EMPTY_LIST);

        mockMvc.perform(get(BEER_API_URL_PATH).contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    void whenDELETEMethodCallReturnNOTFOUND() throws Exception {
        BeerDTO beerDTOToCreate = BeerDTOBuilder.builder().build().toBeerDTO();

        doNothing().when(beerService).deleteById(beerDTOToCreate.getId());

        mockMvc.perform(delete(BEER_API_URL_PATH + "/" + beerDTOToCreate.getId())
                .contentType("application/json")).andExpect(status().isNoContent());
    }

    @Test
    void whenDELETEMethodCallWithInvalidIdReturnStatus() throws Exception {
        BeerDTO beerDTOToCreate = BeerDTOBuilder.builder().build().toBeerDTO();
        Long invalidId = 2L;

        doThrow(BeerNotFoundException.class).when(beerService).deleteById(invalidId);

        mockMvc.perform(delete(BEER_API_URL_PATH + "/" + invalidId)
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHMethodCallReturnStatusOK() throws Exception {
        BeerDTO beerDTOToCreate = BeerDTOBuilder.builder().build().toBeerDTO();
        QuantityDTO quantityDTO = QuantityDTO.builder().build();
        quantityDTO.setQuantity(60);
        beerDTOToCreate.setQuantity(beerDTOToCreate.getQuantity() + quantityDTO.getQuantity());

        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTOToCreate);

        mockMvc.perform(MockMvcRequestBuilders.patch(BEER_API_URL_PATH + "/" + beerDTOToCreate.getId() + BEER_API_SUBPATH_INCREMENT_URL)
                .contentType("application/json").content(asJsonString(quantityDTO))).andExpect(status().isOk());
    }

}
