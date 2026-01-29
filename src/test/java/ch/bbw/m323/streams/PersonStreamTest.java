package ch.bbw.m323.streams;

import ch.bbw.m323.streams.PersonStreamTest.Person.Country;
import ch.bbw.m323.streams.PersonStreamTest.Person.Gender;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

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


}
