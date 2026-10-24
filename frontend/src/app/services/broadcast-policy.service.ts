import { Injectable } from '@angular/core';
import { RequestCategory, UrgencyLevel } from '../models/request.model';

@Injectable({
  providedIn: 'root',
})
export class BroadcastPolicyService {
  readonly vehicleTransportTtlHours = 4;
  readonly roomTtlHours = 4;
  readonly urgentTtlMinutes = 30;
  readonly emergencyTtlMinutes = 15;
  readonly nightPauseStartHour = 21; // 9:00 PM
  readonly nightPauseEndHour = 9;    // 9:00 AM

  /**
   * Calculates the exact expiration ISO string based on the approved rules.
   */
  calculateExpiration(
    category: RequestCategory,
    urgencyLevel: UrgencyLevel,
    scheduledDate: string,
    scheduledTime: string,
    now: Date = new Date()
  ): { expiresAt: Date; activeWindowHours: number; isNightPaused: boolean } {
    // 1. Emergency fast-response (15 mins 24/7)
    if (category === 'emergency' || urgencyLevel === 'emergency') {
      const exp = new Date(now.getTime() + this.emergencyTtlMinutes * 60000);
      return { expiresAt: exp, activeWindowHours: 0.25, isNightPaused: false };
    }

    // 2. Urgent request check: if scheduled departure is within 3 hours
    let isImmediate = false;
    if (scheduledDate && scheduledTime) {
      const scheduledDt = new Date(`${scheduledDate}T${scheduledTime}`);
      const diffHours = (scheduledDt.getTime() - now.getTime()) / (1000 * 60 * 60);
      if (diffHours >= 0 && diffHours <= 3) {
        isImmediate = true;
      }
    }

    if (urgencyLevel === 'urgent' || isImmediate) {
      const exp = new Date(now.getTime() + this.urgentTtlMinutes * 60000);
      return { expiresAt: exp, activeWindowHours: 0.5, isNightPaused: false };
    }

    // 3. Normal TTL with overnight pause logic
    const windowHours = category === 'room' ? this.roomTtlHours : this.vehicleTransportTtlHours;
    const currentHour = now.getHours();

    if (currentHour >= this.nightPauseStartHour) {
      // Created after 9 PM: window starts 9 AM next morning
      const nextMorning = new Date(now);
      nextMorning.setDate(nextMorning.getDate() + 1);
      nextMorning.setHours(this.nightPauseEndHour, 0, 0, 0);
      const exp = new Date(nextMorning.getTime() + windowHours * 3600000);
      return { expiresAt: exp, activeWindowHours: windowHours, isNightPaused: true };
    } else if (currentHour < this.nightPauseEndHour) {
      // Created before 9 AM: window starts 9 AM today
      const todayMorning = new Date(now);
      todayMorning.setHours(this.nightPauseEndHour, 0, 0, 0);
      const exp = new Date(todayMorning.getTime() + windowHours * 3600000);
      return { expiresAt: exp, activeWindowHours: windowHours, isNightPaused: true };
    } else {
      // Normal daytime calculation
      const exp = new Date(now.getTime() + windowHours * 3600000);
      let isNightPaused = false;
      if (exp.getHours() >= this.nightPauseStartHour) {
        // Paused at 9 PM, resumes 9 AM next morning
        const hoursBeforeNight = this.nightPauseStartHour - currentHour;
        const remainingHours = windowHours - hoursBeforeNight;
        const nextMorning = new Date(now);
        nextMorning.setDate(nextMorning.getDate() + 1);
        nextMorning.setHours(this.nightPauseEndHour + remainingHours, 0, 0, 0);
        return { expiresAt: nextMorning, activeWindowHours: windowHours, isNightPaused: true };
      }
      return { expiresAt: exp, activeWindowHours: windowHours, isNightPaused };
    }
  }

  /**
   * Returns human-readable countdown string (e.g. "3h 45m" or "18m").
   */
  getRemainingTimeString(expiresAtIso: string): { text: string; isExpired: boolean; minutesRemaining: number } {
    if (!expiresAtIso) {
      return { text: '--', isExpired: false, minutesRemaining: 0 };
    }

    const expTime = new Date(expiresAtIso).getTime();
    const nowTime = new Date().getTime();
    const diffMs = expTime - nowTime;

    if (diffMs <= 0) {
      return { text: 'Expired', isExpired: true, minutesRemaining: 0 };
    }

    const totalMinutes = Math.floor(diffMs / 60000);
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;

    let text = '';
    if (hours > 0) {
      text += `${hours}h `;
    }
    text += `${minutes}m`;

    return { text: text.trim(), isExpired: false, minutesRemaining: totalMinutes };
  }
}

