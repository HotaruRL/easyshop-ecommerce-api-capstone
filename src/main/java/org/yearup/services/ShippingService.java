package org.yearup.services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.yearup.models.Profile;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShippingService {
    private WebClient webClient;
    private final String SHIPPO_API_KEY = "shippo_test_515d847361ad8b5ee01b0bfe3dcbb923c06450ec";

    public ShippingService(WebClient webClient) {
        this.webClient = webClient;
    }

    public BigDecimal getCheapestShippingRate(Profile profile){
        String fullname = profile.getFirstName() + profile.getLastName();

        // ---Set up for the requestBody---
        // To Address
        HashMap<String, Object> addressToMap = new HashMap<>();
        addressToMap.put("name", fullname);
        addressToMap.put("company", fullname);
        addressToMap.put("street1", profile.getAddress());
        addressToMap.put("city", profile.getCity());
        addressToMap.put("state", profile.getState());
        addressToMap.put("zip", profile.getZip());
        addressToMap.put("country", "US");

        // From Address
        HashMap<String, Object> addressFromMap = new HashMap<>();
        addressFromMap.put("name", "Hau Luc");
        addressFromMap.put("company", "HotaruRL, Inc.");
        addressFromMap.put("street1", "4550 11th Ave NE");
        addressFromMap.put("city", "Seattle");
        addressFromMap.put("state", "WA");
        addressFromMap.put("zip", "98105");
        addressFromMap.put("country", "US");

        // Parcel
        HashMap<String, Object> parcelMap = new HashMap<>();
        parcelMap.put("length", "5");
        parcelMap.put("width", "5");
        parcelMap.put("height", "5");
        parcelMap.put("distance_unit", "in");
        parcelMap.put("weight", "2");
        parcelMap.put("mass_unit", "lb");

        // Shipment - requestBody
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("address_to", addressToMap);
        requestBody.put("address_from", addressFromMap);
        requestBody.put("parcels", List.of(parcelMap));
        requestBody.put("async", false);

        // create a Mono - a promise - of a Map
        Mono<Map> responseMono = this.webClient
                .post()
                .uri("/shipments/")
                .header("Authorization", "ShippoToken " + SHIPPO_API_KEY)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class);

        try {
            // .block() unwraps the Mono, turns it into a Map
            Map response = responseMono.block();

            // for debugging
            System.out.println("Shippo API Response:" + response);

            // access the "rates" key directly from Map
            List<Map<String, String>> rates = (List<Map<String, String>>) response.get("rates");

            if (rates != null && !rates.isEmpty()){
                BigDecimal cheapestRate = new BigDecimal(rates.get(0).get("amount"));

                // loop to find the cheapest rate
                for (Map<String, String> currentRateMap : rates){
                    BigDecimal currentRate = new BigDecimal(currentRateMap.get("amount"));

                    // compare() returns -1 if currentRate is smaller
                    if (currentRate.compareTo(cheapestRate) < 0){
                        // update currentRate as cheapestRate
                        cheapestRate = currentRate;
                    }
                }
                return cheapestRate;
            }
        } catch (Exception e){
            System.err.println("Error calling shipping API:" + e.getMessage());
        }
        // return default value if something went wrong
        return new BigDecimal("25.00");
    }
}
