import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/api'

export interface Shift {
  id: number
  userId: number
  userName: string
  data: string
  inizio: string
  fine: string
}

export const DAYS = ['Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab', 'Dom'] as const

export const usePlannerStore = defineStore('planner', () => {
  const shifts = ref<Shift[]>([])

  function getShifts(dateKey: string): Shift[] {
    return shifts.value.filter(s => s.data === dateKey)
  }

  async function carica(dal: string, al: string) {
    shifts.value = await api.get<Shift[]>(`/api/turni?dal=${dal}&al=${al}`)
  }

  async function addShift(data: string, userId: number, inizio: string, fine: string) {
    const created = await api.post<Shift>('/api/turni', { userId, data, inizio, fine })
    shifts.value.push(created)
  }

  async function removeShift(shiftId: number) {
    await api.delete(`/api/turni/${shiftId}`)
    shifts.value = shifts.value.filter(s => s.id !== shiftId)
  }

  return { shifts, getShifts, carica, addShift, removeShift }
})

export function toDateKey(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const g = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${g}`
}

export function weekDates(offset: number): Date[] {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  d.setDate(d.getDate() - ((d.getDay() + 6) % 7) + offset * 7)
  return Array.from({ length: 7 }, (_, i) => {
    const day = new Date(d)
    day.setDate(d.getDate() + i)
    return day
  })
}

export function monthDates(offset: number): Date[] {
  const now = new Date()
  const first = new Date(now.getFullYear(), now.getMonth() + offset, 1)
  const last = new Date(now.getFullYear(), now.getMonth() + offset + 1, 0)
  const dates: Date[] = []
  for (let d = new Date(first); d <= last; d.setDate(d.getDate() + 1))
    dates.push(new Date(d))
  return dates
}

export function weekLabel(offset: number): string {
  const dates = weekDates(offset)
  const fmt = (d: Date) => d.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: '2-digit' })
  return `${fmt(dates[0])} – ${fmt(dates[6])}`
}

export function monthLabel(offset: number): string {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth() + offset, 1)
    .toLocaleDateString('it-IT', { month: 'long', year: 'numeric' })
}
