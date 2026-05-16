import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Shift {
  id: number
  userId: number
  name: string
  start: string
  end: string
}

// weekKey = 'YYYY-Www'  (e.g. '2025-W20')
type WeekKey = string
type DayKey  = 'Lun' | 'Mar' | 'Mer' | 'Gio' | 'Ven' | 'Sab' | 'Dom'
export const DAYS: DayKey[] = ['Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab', 'Dom']

export const members: { id: number; name: string }[] = []

export const usePlannerStore = defineStore('planner', () => {
  const data = ref<Record<WeekKey, Partial<Record<DayKey, Shift[]>>>>({})
  let nextId = 1

  function getShifts(week: WeekKey, day: DayKey): Shift[] {
    return data.value[week]?.[day] ?? []
  }

  function addShift(week: WeekKey, day: DayKey, userId: number, start: string, end: string) {
    const member = members.find(m => m.id === userId)
    if (!member) return
    if (!data.value[week]) data.value[week] = {}
    if (!data.value[week][day]) data.value[week][day] = []
    data.value[week][day]!.push({ id: nextId++, userId, name: member.name, start, end })
  }

  function removeShift(week: WeekKey, day: DayKey, shiftId: number) {
    if (!data.value[week]?.[day]) return
    data.value[week][day] = data.value[week][day]!.filter(s => s.id !== shiftId)
  }

  return { data, getShifts, addShift, removeShift }
})
