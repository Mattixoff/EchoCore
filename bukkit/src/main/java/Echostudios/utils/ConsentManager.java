package Echostudios.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConsentManager {
	private static final long DEFAULT_CONSENT_MS = 60_000L; // 60 seconds
	private static final Map<UUID, Long> inventoryConsentUntil = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> enderchestConsentUntil = new ConcurrentHashMap<>();

	private ConsentManager() {}

	public static void grantInventoryConsent(UUID playerId) {
		grantInventoryConsent(playerId, DEFAULT_CONSENT_MS);
	}

	public static void grantInventoryConsent(UUID playerId, long durationMs) {
		inventoryConsentUntil.put(playerId, System.currentTimeMillis() + Math.max(0L, durationMs));
	}

	public static void grantEnderchestConsent(UUID playerId) {
		grantEnderchestConsent(playerId, DEFAULT_CONSENT_MS);
	}

	public static void grantEnderchestConsent(UUID playerId, long durationMs) {
		enderchestConsentUntil.put(playerId, System.currentTimeMillis() + Math.max(0L, durationMs));
	}

	public static boolean hasInventoryConsent(UUID playerId) {
		return isActive(inventoryConsentUntil, playerId);
	}

	public static boolean hasEnderchestConsent(UUID playerId) {
		return isActive(enderchestConsentUntil, playerId);
	}

	private static boolean isActive(Map<UUID, Long> map, UUID playerId) {
		Long until = map.get(playerId);
		if (until == null) return false;
		if (System.currentTimeMillis() <= until) return true;
		map.remove(playerId);
		return false;
	}
}
