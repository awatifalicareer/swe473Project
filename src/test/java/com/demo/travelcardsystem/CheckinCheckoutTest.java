package com.demo.travelcardsystem;

import com.demo.travelcardsystem.constant.TransportType;
import com.demo.travelcardsystem.entity.TravelCard;
import com.demo.travelcardsystem.model.request.SwipeRequest;
import com.demo.travelcardsystem.repository.InMemoryCardTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CheckinCheckoutTest extends IntegrationTest {

    private static final String APPLICATION_JSON = "application/json";
    private static final String SWIPE_ENDPOINT = "/api/card/swipe";
    private static final String CARD_BALANCE_ENDPOINT = "/api/card/{cardNumber}";

    private static final String ALGUBAIBA = "Algubaiba";
    private static final String JUMEIRAH = "Jumeirah";
    private static final String BUR_DUBAI = "Bur Dubai";
    private static final String DEIRA = "Deira";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryCardTransactionRepository inMemoryCardTransactionRepository;

    @Autowired
    private TravelHelperTest travelHelperTest;

    @BeforeEach
    public void resetRepository() {
        inMemoryCardTransactionRepository.clearTravelCardStore();
    }

    @DisplayName("User take few trips and check balance at end of the trip")
    @Test
    public void user_take_trip_and_check_balance() throws Exception {

        // GIVEN - User/Travel-card exists in the system
        TravelCard travelCard = travelHelperTest.directUserRegistration("1A101", 30);
        SwipeRequest swipeRequest;

        // AND - User takes train journey
        swipeRequest = travelHelperTest.prepareSwipeRequest(
                travelCard.getCardNumber(),
                ALGUBAIBA,
                TransportType.TRAIN
        );

        mockMvc.perform(post(SWIPE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(swipeRequest)))
                .andExpect(status().isOk());

        swipeRequest = travelHelperTest.prepareSwipeRequest(
                travelCard.getCardNumber(),
                JUMEIRAH,
                TransportType.TRAIN
        );

        mockMvc.perform(post(SWIPE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(swipeRequest)))
                .andExpect(status().isOk());

        System.out.println(
                "Card balance after journey-1 is -> "
                        + inMemoryCardTransactionRepository
                        .findCardByCardNumber(travelCard.getCardNumber())
                        .getBalance()
        );

        // AND - User takes bus journey
        swipeRequest = travelHelperTest.prepareSwipeRequest(
                travelCard.getCardNumber(),
                JUMEIRAH,
                TransportType.BUS
        );

        mockMvc.perform(post(SWIPE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(swipeRequest)))
                .andExpect(status().isOk());

        swipeRequest = travelHelperTest.prepareSwipeRequest(
                travelCard.getCardNumber(),
                BUR_DUBAI,
                TransportType.BUS
        );

        mockMvc.perform(post(SWIPE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(swipeRequest)))
                .andExpect(status().isOk());

        System.out.println(
                "Card balance after journey-2 is -> "
                        + inMemoryCardTransactionRepository
                        .findCardByCardNumber(travelCard.getCardNumber())
                        .getBalance()
        );

        // AND - User takes another train journey
        swipeRequest = travelHelperTest.prepareSwipeRequest(
                travelCard.getCardNumber(),
                BUR_DUBAI,
                TransportType.TRAIN
        );

        mockMvc.perform(post(SWIPE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(swipeRequest)))
                .andExpect(status().isOk());

        swipeRequest = travelHelperTest.prepareSwipeRequest(
                travelCard.getCardNumber(),
                DEIRA,
                TransportType.TRAIN
        );

        mockMvc.perform(post(SWIPE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(swipeRequest)))
                .andExpect(status().isOk());

        // THEN - Verify balance at the end of the journey
        mockMvc.perform(get(CARD_BALANCE_ENDPOINT, travelCard.getCardNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("23.45"));
    }
}