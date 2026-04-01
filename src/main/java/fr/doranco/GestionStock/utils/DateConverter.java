package fr.doranco.GestionStock.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateConverter {

	private static final String DATE_PATTERN = "dd-MM-yyyy";
	private static final DateTimeFormatter FORMATTER =
			DateTimeFormatter.ofPattern(DATE_PATTERN);

	private DateConverter() {
		// Empêche l’instanciation
	}

	/* ============================
       String → LocalDate
       ============================ */
	public static LocalDate toLocalDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			return LocalDate.parse(value, FORMATTER);
		} catch (DateTimeParseException ex) {
			throw new IllegalArgumentException(
					"Invalid date format. Expected " + DATE_PATTERN + " but got: " + value,
					ex
			);
		}
	}

	/* ============================
       LocalDate → String
       ============================ */
	public static String toString(LocalDate date) {
		if (date == null) {
			return null;
		}

		return date.format(FORMATTER);
	}
}