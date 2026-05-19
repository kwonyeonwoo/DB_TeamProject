export const scheduleTypes = [
  { value: 1, label: '수업', className: 'lecture' },
  { value: 2, label: '과제', className: 'assignment' },
  { value: 3, label: '시험', className: 'exam' },
  { value: 4, label: '스터디', className: 'study' },
  { value: 5, label: '기타', className: 'etc' },
] as const

export function getScheduleType(type: number) {
  return scheduleTypes.find((item) => item.value === type) ?? scheduleTypes[4]
}

export function getScheduleTypeLabel(type: number) {
  return getScheduleType(type).label
}
