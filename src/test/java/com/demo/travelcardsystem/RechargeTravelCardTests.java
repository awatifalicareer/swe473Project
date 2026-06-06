package com.demo.travelcardsystem;

import com.demo.travelcardsystem.entity.TravelCard;
import com.demo.travelcardsystem.repository.InMemoryCardTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RechargeTravelCardTests extends IntegrationTest {

    private static final String APPLICATION_JSON = "application/json";
    private static final String REGISTER_ENDPOINT = "/api/card/register";
    private static final String PING_ENDPOINT = "/api/card/ping";
    private static final String RECHARGE_ENDPOINT = "/api/card/recharge/{rechargeAmount}";
    private static final String INVALID_CARD_MESSAGE =
            "This card is Invalid. Please use a valid card";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryCardTransactionRepository inMemoryCardTransactionRepository;

    @Autowired
    private TravelHelperTest travelHelperTest;

    private static Stream<Arguments> usersGenerator() {
        return Stream.of(
                Arguments.of("1AE101", 30),
                Arguments.of("1AE102", 40)
        );
    }

    @BeforeEach
    public void resetRepository() {
        inMemoryCardTransactionRepository.clearTravelCardStore();
    }

    @DisplayName("Service is Up and Running")
    @Test
    public void check_if_ping_is_working() throws Exception {
        mockMvc.perform(get(PING_ENDPOINT))
                .andExpect(status().isOk());
    }

    @DisplayName("User try to register himself successfully")
    @ParameterizedTest
    @MethodSource("usersGenerator")
    public void register_user_in_the_system(String cardNumber, double amount) throws Exception {

        // GIVEN - user enters card details
        TravelCard travelCard = new TravelCard();
        travelCard.setCardNumber(cardNumber);
        travelCard.setBalance(amount);

        // WHEN - user registers the card
        mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelCard)))
                .andExpect(status().isOk());

        // THEN - card should be stored in the repository
        assertThat(
                inMemoryCardTransactionRepository.findCardByCardNumber(cardNumber)
        ).isEqualTo(travelCard);
    }

    @DisplayName("User try to recharge a invalid card. System throws INVALID_CARD exception")
    @Test
    public void register_user_with_invalid_card_number() throws Exception {

        // GIVEN - invalid card number
        TravelCard travelCard = new TravelCard();
        travelCard.setCardNumber(null);
        travelCard.setBalance(30);

        // WHEN - user attempts registration
        String errorMsg = mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(travelCard)))
                .andExpect(status().isNotAcceptable())
                .andReturn()
                .getResolvedException()
                .getMessage();

        // THEN - appropriate exception message should be returned
        assertEquals(INVALID_CARD_MESSAGE, errorMsg);
    }

    @DisplayName("Users are able to recharge the card successfully")
    @ParameterizedTest
    @MethodSource("usersGenerator")
    public void users_are_able_to_recharge_the_card_successfully(
            String cardNumber,double amount) throws Exception {

        // GIVEN - user exists in the system
        travelHelperTest.directUserRegistration(cardNumber, 0);

        // WHEN - user recharges the card
        mockMvc.perform(post(RECHARGE_ENDPOINT, amount)
                        .contentType(APPLICATION_JSON)
                        .content(cardNumber))
                .andExpect(status().isOk());

        // THEN - balance should be updated
        assertEquals(
                amount,
                inMemoryCardTransactionRepository
                        .findCardByCardNumber(cardNumber)
                        .getBalance()
        );
    }
}