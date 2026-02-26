package ch.bbw.m323.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

class GarageStreamTest implements WithAssertions {

	Inventory inventory;

	record Inventory(List<Customer> products) {

		record Customer(String id, String customer, String email, List<Car> cars) {

			record Car(String brand, String price, Wheel wheels, Radio radio) {

				record Wheel(String brand, Integer amount) {}

				record Radio(Boolean ukw, Bluetooth bluetooth) {

					record Bluetooth(Integer version, List<Standard> standards) {

						record Standard(String codec, Boolean partial) {}
					}
				}
			}
		}
	}

	@BeforeEach
	void readJson() throws IOException {
		// TODO: change to "manynull.json" for a harder experience
		try (var in = GarageStreamTest.class.getClassLoader().getResourceAsStream("manynull.json")) {
			inventory = new ObjectMapper().readValue(in, Inventory.class);
		}
	}

	@Test
	void namesOfCostumersWith2orMoreCars() {
		Predicate<Inventory.Customer> customerWithMoreThanCars = customer ->
				Optional.ofNullable(customer.cars()).map(List::size).orElse(0) >= 2;
		assertThat(Optional.ofNullable(inventory.products()).stream()
				.flatMap(List::stream)
				.filter(customerWithMoreThanCars)
				.map(Inventory.Customer::customer)
				.toList()).hasSizeBetween(10, 11);
	}
	@Test
	void allCarsWithUKWRadio() {
		Predicate<Inventory.Customer.Car> carsWithUKWRadio = car ->
				Optional.ofNullable(car.radio()).map(Inventory.Customer.Car.Radio::ukw).orElse(false);
		assertThat(Optional.ofNullable(inventory.products()).stream()
				.flatMap(List::stream)
				.flatMap(customer -> Optional.ofNullable(customer.cars()).stream().flatMap(List::stream))
				.filter(Objects::nonNull) // Exclude null cars
				.filter(carsWithUKWRadio)
				.count()).isIn(8L, 16L);
	}

}
