package ch.bbw.m323.streams;

import ch.bbw.m323.streams.PersonStreamTest.Person.Country;
import ch.bbw.m323.streams.PersonStreamTest.Person.Gender;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class PersonStreamTest implements WithAssertions {

	record Person(String name, int age, Gender gender, Country country) {

		enum Gender {
			MALE, FEMALE, NON_BINARY
		}

		record Country(String name, long population) {
		}

		public boolean isAdult() {
			return age >= 18;
		}
	}

	final Country france = new Country("France", 65_235_184L);

	final Country canada = new Country("Canada", 37_653_095L);

	final Country uk = new Country("United Kingdom", 67_791_734L);

	final List<Person> people = List.of(
			new Person("Brent", 50, Gender.MALE, canada),
			new Person("Luca", 22, Gender.MALE, canada),
			new Person("May", 12, Gender.FEMALE, france),
			new Person("Jojo", 23, Gender.NON_BINARY, uk),
			new Person("Maurice", 15, Gender.MALE, france),
			new Person("Alice", 15, Gender.FEMALE, france),
			new Person("Laurence", 22, Gender.MALE, france),
			new Person("Samantha", 67, Gender.FEMALE, canada));

	// tag::sample[]
	@Test
	void allNamesUppercase() { // Alle Namen UPPERCASE.
		// Dies ist eine Beispielimplementation, wie eine Lösung auszusehen hat.
		// Die Spielregel wurde eingehalten: nur ein `;` am Ende der Funktion
		assertThat(people.stream() // ein Stream<Person>
				.map(Person::name) // ein Stream<String> mit allen Namen. Dasselbe wie `.map(x -> x,name())`.
				.map(String::toUpperCase) // ein Stream<String> mit UPPERCASE-Namen
				.toList() // eine List<String>
		).containsExactly("BRENT", "LUCA", "MAY", "JOJO", "MAURICE", "ALICE", "LAURENCE", "SAMANTHA");
	}

	@Test
	void allNamesMaxFourChars() {
		assertThat(people.stream().map(Person::name).filter(name -> name.length() <= 4).toList()
		).containsOnly("Luca", "May", "Jojo");

	}

	@Test
	void sumOfAllAges() {
		assertThat(people.stream().mapToInt(Person::age).sum()
		).isEqualTo(226);
	}

	@Test
	void ageOfOldestPerson() {
		assertThat(people.stream().mapToInt(Person::age).max().orElse(0)
		).isEqualTo(67);
	}

	@Test
	void allMaleCanadians() {
		assertThat(people.stream()
				.filter(person -> person.gender() == Gender.MALE && person.country().equals(canada))
				.toList()
		).hasSize(2).allSatisfy(x -> assertThat(x).isInstanceOf(Person.class));
	}

	@Test
	void allNamesConnectedWithUnderline() {
		assertThat(people.stream()
				.map(Person::name)
				.collect(Collectors.joining("_"))
		).hasSize(51).contains("_");
	}

	@Test
	void allWomenFromCountryWithMaxOneMilPopulation() {
		assertThat(people.stream().filter(person -> person.gender()
				.equals(Gender.FEMALE) && person.country()
				.population() <= 1_000_000L).toList()
		).isEmpty();
	}

	@Test
	void allMenSortedByAge() {
		assertThat(people.stream()
				.filter(person -> person.gender() == Gender.MALE)
				.sorted((p1, p2) -> Integer.compare(p1.age(), p2.age()))
				.map(Person::name)
				.toList()
		).containsExactly("Maurice", "Luca", "Laurence", "Brent");
	}

	@Test
	void secondOldestFemalePersonWithLimit() {
		assertThat(people.stream()
				.filter(person -> person.gender() == Gender.FEMALE)
				.sorted((p1, p2) -> Integer.compare(p2.age(), p1.age())) // Sort by age descending
				.skip(1) // Skip the oldest
				.limit(1) // Limit to the second oldest
				.findFirst() // Get the second oldest
		).get().extracting(Person::name).isEqualTo("Alice"); // Ensure the result is present
	}

	@Test
	void allEvenAgesWithoutAge22andBetween0And100() {
		assertThat(IntStream.range(0, 100)
				.filter(number -> number % 2 == 0)
				.boxed()
				.filter(number -> !people.stream().map(Person::age).toList().contains(number))
				.toList()
		).hasSize(47).contains(0, 62, 98).doesNotContain(22);
	}

	@Test
	void allLettersOfAllNameInAlphabeticalOrderWithFlatMap() {
		assertThat(people.stream()
				.map(Person::name)
				.flatMap(name -> name.chars().mapToObj(c -> (char) c)).distinct()
				.sorted()
				.map(String::valueOf)
				.collect(Collectors.joining())
		).isEqualTo("ABJLMSacehijlmnortuy");
	}

	@Test
	void listOfNamesInAlphabeticalOrderFirstAscThenDesc() {
		List<String> combined = people.stream()
				.map(Person::name)
				.sorted()
				.collect(Collectors.collectingAndThen(
						Collectors.toList(),
						sortedAsc -> {
							List<String> sortedDesc = new ArrayList<>(sortedAsc);
							sortedDesc.sort(Comparator.reverseOrder());
							sortedAsc.addAll(sortedDesc);
							return sortedAsc;
						}
				));

		assertThat(combined)
				.hasSize(people.size() * 2)
				.startsWith("Alice")
				.endsWith("Alice");
	}

}
