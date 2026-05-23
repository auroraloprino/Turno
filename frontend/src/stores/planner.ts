import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Shift {
  id: number
  userId: number
  name: string
  start: string
  end: string
}

export const DAYS = ['Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab', 'Dom'] as const

export const members: { id: number; name: string }[] = []

export const usePlannerStore = defineStore('planner', () => {
  const data = ref<Record<string, Shift[]>>({})
  let nextId = 1

  function getShifts(dateKey: string): Shift[] {
    return data.value[dateKey] ?? []
  }

  function addShift(dateKey: string, userId: number, start: string, end: string) {
    const member = members.find(m => m.id === userId)
    if (!member) return
    if (!data.value[dateKey]) data.value[dateKey] = []
    data.value[dateKey].push({ id: nextId++, userId, name: member.name, start, end })
  }

  function removeShift(dateKey: string, shiftId: number) {
    if (!data.value[dateKey]) return
    data.value[dateKey] = data.value[dateKey].filter(s => s.id !== shiftId)
  }

  return { data, getShifts, addShift, removeShift }
})

export function toDateKey(d: Date): string {
  return d.toISOString().slice(0, 10)
}

export function weekStart(offset: number): Date {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  d.setDate(d.getDate() - ((d.getDay() + 6) % 7) + offset * 7)
  return d
}

export function weekDates(offset: number): Date[] {
  const mon = weekStart(offset)
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(mon)
    d.setDate(mon.getDate() + i)
    return d
  })
}

export function monthDates(offset: number): Date[] {
  const now = new Date()
  const year  = now.getFullYear()
  const month = now.getMonth() + offset
  const first = new Date(year, month, 1)
  const last  = new Date(year, month + 1, 0)
  const dates: Date[] = []
  for (let d = new Date(first); d <= last; d.setDate(d.getDate() + 1))
    dates.push(new Date(d))
  return dates
}

export function monthLabel(offset: number): string {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth() + offset, 1)
    .toLocaleDateString('it-IT', { month: 'long', year: 'numeric' })
}

export function weekLabel(offset: number): string {
  const dates = weekDates(offset)
  const fmt = (d: Date) => d.toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: '2-digit' })
  return `${fmt(dates[0])} – ${fmt(dates[6])}`
}
