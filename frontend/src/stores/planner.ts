import { defineStore } from "pinia";
import { ref } from "vue";
import { api } from "@/api";
import { conNomi } from "@/composables/useUtenti";

export interface Shift {
  id: number;
  userId: number;
  userName?: string;
  data: string;
  inizio: string;
  fine: string;
}

export interface NewShift {
  userId: number;
  data: string;
  inizio: string;
  fine: string;
}

export const hhmm = (t: string) => t.slice(0, 5);

export const DAYS = ["Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"] as const;

export const usePlannerStore = defineStore("planner", () => {
  const shifts = ref<Shift[]>([]);

  function getShifts(dateKey: string): Shift[] {
    return shifts.value.filter((s) => s.data === dateKey);
  }

  async function carica(dal: string, al: string) {
    shifts.value = await api.get<Shift[]>(`/api/turni?dal=${dal}&al=${al}`).then((lista) => conNomi(lista));
  }

  async function addShift(
    data: string,
    userId: number,
    inizio: string,
    fine: string,
  ) {
    const created = await api.post<Shift>("/api/turni", {
      userId,
      data,
      inizio,
      fine,
    });
    shifts.value.push(created);
  }

  async function removeShift(shiftId: number) {
    await api.delete(`/api/turni/${shiftId}`);
    shifts.value = shifts.value.filter((s) => s.id !== shiftId);
  }

  async function addMany(list: NewShift[]) {
    const res = await api.post<{ creati: Shift[]; saltati: number }>(
      "/api/turni/batch",
      list,
    );
    shifts.value.push(...res.creati);
    return res;
  }

  return { shifts, getShifts, carica, addShift, addMany, removeShift };
});

export function toDateKey(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const g = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${g}`;
}

export function weekDates(offset: number): Date[] {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  d.setDate(d.getDate() - ((d.getDay() + 6) % 7) + offset * 7);
  return Array.from({ length: 7 }, (_, i) => {
    const day = new Date(d);
    day.setDate(d.getDate() + i);
    return day;
  });
}

// new Date("2026-11-16") verrebbe letto come UTC e in alcuni fusi orari slitterebbe di un giorno
export function fromDateKey(key: string): Date {
  const [y, m, d] = key.split("-").map(Number);
  return new Date(y, m - 1, d);
}

export function weekOffsetOf(date: Date): number {
  const monday = (d: Date) => {
    const m = new Date(d);
    m.setHours(0, 0, 0, 0);
    m.setDate(m.getDate() - ((m.getDay() + 6) % 7));
    return m;
  };
  const diffDays =
    (monday(date).getTime() - monday(new Date()).getTime()) / 86_400_000;
  return Math.round(diffDays / 7);
}

export function monthDates(offset: number): Date[] {
  const now = new Date();
  const first = new Date(now.getFullYear(), now.getMonth() + offset, 1);
  const last = new Date(now.getFullYear(), now.getMonth() + offset + 1, 0);
  const dates: Date[] = [];
  for (let d = new Date(first); d <= last; d.setDate(d.getDate() + 1))
    dates.push(new Date(d));
  return dates;
}

export function weekLabel(offset: number): string {
  const dates = weekDates(offset);
  const fmt = (d: Date) =>
    d.toLocaleDateString("it-IT", {
      day: "2-digit",
      month: "2-digit",
      year: "2-digit",
    });
  return `${fmt(dates[0])} – ${fmt(dates[6])}`;
}

export function monthLabel(offset: number): string {
  const now = new Date();
  return new Date(
    now.getFullYear(),
    now.getMonth() + offset,
    1,
  ).toLocaleDateString("it-IT", { month: "long", year: "numeric" });
}
