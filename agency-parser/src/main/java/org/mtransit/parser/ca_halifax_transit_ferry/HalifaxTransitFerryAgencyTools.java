package org.mtransit.parser.ca_halifax_transit_ferry;

import static org.mtransit.commons.RegexUtils.DIGITS;
import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://www.halifax.ca/home/open-data
// https://data-hrm.hub.arcgis.com/pages/open-data-downloads#section-3
public class HalifaxTransitFerryAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new HalifaxTransitFerryAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_EN;
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Halifax Transit";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_FERRY;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Nullable
	@Override
	public Long convertRouteIdFromShortNameNotSupported(@NotNull String routeShortName) {
		switch (routeShortName) {
		case "FerD":
		case "ALD":
			return 100_001L;
		case "FerW":
		case "WS":
			return 100_002L;
		}
		return super.convertRouteIdFromShortNameNotSupported(routeShortName);
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanRouteShortName(@NotNull String routeShortName) {
		switch (routeShortName) {
		case "FerD":
			return "ALD";
		case "FerW":
			return "WS";
		}
		return super.cleanRouteShortName(routeShortName);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR_BLUE = "00558C"; // BLUE (web site CSS)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@Nullable
	@Override
	public String fixColor(@Nullable String color) {
		if ("7476D9".equalsIgnoreCase(color)) { // purple
			return AGENCY_COLOR;
		}
		return super.fixColor(color);
	}

	@NotNull
	@Override
	public String cleanStopOriginalId(@NotNull String gStopId) {
		gStopId = CleanUtils.cleanMergedID(gStopId);
		return gStopId;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign);
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = CleanUtils.SAINT.matcher(tripHeadsign).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern FERRY_STOP = Pattern.compile("(ferry stop - )", Pattern.CASE_INSENSITIVE);
	private static final Pattern ENDS_WITH_NUMBER = Pattern.compile("( \\([\\d]+\\)$)", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = FERRY_STOP.matcher(gStopName).replaceAll(EMPTY);
		gStopName = ENDS_WITH_NUMBER.matcher(gStopName).replaceAll(EMPTY);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		final String stopId = gStop.getStopId();
		if (CharUtils.isDigitsOnly(stopId)) {
			return Integer.parseInt(stopId);
		}
		final Matcher matcher = DIGITS.matcher(stopId);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group());
		}
		throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop);
	}

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		//noinspection deprecation
		final String stopId = gStop.getStopId();
		if (CharUtils.isDigitsOnly(stopId)) {
			return stopId; // using stop ID as stop code ("GoTime" number)
		}
		final Matcher matcher = DIGITS.matcher(stopId);
		if (matcher.find()) {
			return matcher.group();
		}
		throw new MTLog.Fatal("Unexpected stop code for %s!", gStop);
	}
}
