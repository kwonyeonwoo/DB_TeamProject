import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getErrorMessage } from '../api/errors'
import { schedulesApi } from '../api/schedules'
import type { Schedule, SaveScheduleRequest } from '../api/schedules'

const scheduleTypes = [
  { value: 1, label: '과제', className: 'assignment' },
  { value: 2, label: '시험', className: 'exam' },
  { value: 3, label: '학습', className: 'study' },
  { value: 4, label: '팀플', className: 'team' },
  { value: 5, label: '개인 일정', className: 'personal' },
]

const weekdays = ['일', '월', '화', '수', '목', '금', '토']

function pad(value: number) {
  return String(value).padStart(2, '0')
}

function getDateKey(date: Date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function getMonthLabel(date: Date) {
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월`
}

function toDateTimeLocal(value: string) {
  return value.slice(0, 16)
}

function toIsoLikeLocal(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  })
}

function formatTime(value: string) {
  return new Date(value).toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

function getType(type: number) {
  return scheduleTypes.find((item) => item.value === type) ?? scheduleTypes[4]
}

function createEmptyForm(dateKey = getDateKey(new Date())): SaveScheduleRequest {
  return {
    title: '',
    start_at: `${dateKey}T09:00`,
    end_at: `${dateKey}T10:00`,
    description: '',
    type: 1,
  }
}

function getMonthDays(month: Date) {
  const firstDay = new Date(month.getFullYear(), month.getMonth(), 1)
  const startDate = new Date(firstDay)
  startDate.setDate(firstDay.getDate() - firstDay.getDay())

  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + index)
    return date
  })
}

function getMonthRange(month: Date) {
  const start = new Date(month.getFullYear(), month.getMonth(), 1, 0, 0, 0)
  const end = new Date(month.getFullYear(), month.getMonth() + 1, 0, 23, 59, 59)

  return {
    start_at: `${getDateKey(start)}T00:00:00`,
    end_at: `${getDateKey(end)}T23:59:59`,
  }
}

export function SchedulePage() {
  const [schedules, setSchedules] = useState<Schedule[]>([])
  const [currentMonth, setCurrentMonth] = useState(() => new Date())
  const [selectedDate, setSelectedDate] = useState(() => getDateKey(new Date()))
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingSchedule, setEditingSchedule] = useState<Schedule | null>(null)
  const [formState, setFormState] = useState<SaveScheduleRequest>(() => createEmptyForm())

  const monthDays = useMemo(() => getMonthDays(currentMonth), [currentMonth])
  const monthParams = useMemo(() => getMonthRange(currentMonth), [currentMonth])
  const todayKey = getDateKey(new Date())

  const schedulesByDate = useMemo(() => {
    return schedules.reduce<Record<string, Schedule[]>>((groups, schedule) => {
      const key = getDateKey(new Date(schedule.start_at))
      groups[key] = [...(groups[key] ?? []), schedule]
      return groups
    }, {})
  }, [schedules])

  const selectedSchedules = schedulesByDate[selectedDate] ?? []

  const loadSchedules = useCallback(async () => {
    setIsLoading(true)

    try {
      const response = await schedulesApi.listSchedules(monthParams)
      setSchedules(response.items)
      setErrorMessage(null)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    } finally {
      setIsLoading(false)
    }
  }, [monthParams])

  useEffect(() => {
    let isMounted = true

    schedulesApi
      .listSchedules(monthParams)
      .then((response) => {
        if (!isMounted) {
          return
        }

        setSchedules(response.items)
        setErrorMessage(null)
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return
        }

        setErrorMessage(getErrorMessage(error))
      })
      .finally(() => {
        if (!isMounted) {
          return
        }

        setIsLoading(false)
      })

    return () => {
      isMounted = false
    }
  }, [monthParams])

  const resetFormForDate = (dateKey: string) => {
    setEditingSchedule(null)
    setFormState(createEmptyForm(dateKey))
  }

  const openDateModal = (date: Date) => {
    const dateKey = getDateKey(date)
    setSelectedDate(dateKey)
    resetFormForDate(dateKey)
    setSuccessMessage(null)
    setErrorMessage(null)
    setIsModalOpen(true)
  }

  const openCreateModal = () => {
    const dateKey = todayKey
    setSelectedDate(dateKey)
    resetFormForDate(dateKey)
    setSuccessMessage(null)
    setErrorMessage(null)
    setIsModalOpen(true)
  }

  const openEditForm = (schedule: Schedule) => {
    setEditingSchedule(schedule)
    setFormState({
      title: schedule.title,
      start_at: toDateTimeLocal(schedule.start_at),
      end_at: toDateTimeLocal(schedule.end_at),
      description: schedule.description ?? '',
      type: schedule.type,
    })
    setSuccessMessage(null)
    setErrorMessage(null)
  }

  const closeModal = () => {
    setIsModalOpen(false)
    setEditingSchedule(null)
    setFormState(createEmptyForm(selectedDate))
  }

  const moveMonth = (amount: number) => {
    setIsLoading(true)
    setCurrentMonth(
      (current) => new Date(current.getFullYear(), current.getMonth() + amount, 1),
    )
  }

  const handleSave = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const request = {
      ...formState,
      start_at: toIsoLikeLocal(formState.start_at),
      end_at: toIsoLikeLocal(formState.end_at),
      type: Number(formState.type),
    }

    try {
      if (editingSchedule) {
        await schedulesApi.updateSchedule(editingSchedule.id, request)
        setSuccessMessage('일정을 수정했습니다.')
      } else {
        await schedulesApi.createSchedule(request)
        setSuccessMessage('일정을 등록했습니다.')
      }

      await loadSchedules()
      resetFormForDate(getDateKey(new Date(request.start_at)))
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    }
  }

  const handleDelete = async (schedule: Schedule) => {
    const confirmed = window.confirm('일정을 삭제할까요?')

    if (!confirmed) {
      return
    }

    try {
      await schedulesApi.deleteSchedule(schedule.id)
      setSuccessMessage('일정을 삭제했습니다.')
      setErrorMessage(null)
      await loadSchedules()
      resetFormForDate(selectedDate)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    }
  }

  return (
    <>
      <section className="page-header schedule-page-header">
        <div>
          <p className="eyebrow">일정</p>
          <h1 className="page-title">캘린더</h1>
          <p className="page-description">
            월간 캘린더에서 일정을 한눈에 확인하고, 날짜를 선택해 상세 일정 확인과 수정까지
            이어갈 수 있습니다.
          </p>
        </div>
        <div className="button-row">
          <button className="button secondary" type="button" onClick={() => moveMonth(-1)}>
            이전 달
          </button>
          <button className="button secondary" type="button" onClick={() => moveMonth(1)}>
            다음 달
          </button>
          <button className="button" type="button" onClick={openCreateModal}>
            일정 추가
          </button>
        </div>
      </section>

      {errorMessage && <p className="form-error">{errorMessage}</p>}
      {successMessage && <p className="form-success">{successMessage}</p>}

      <section className="calendar-shell" aria-label="월간 일정 캘린더">
        <aside className="calendar-sidebar">
          <div className="mini-calendar-card">
            <span className="calendar-chip">이번 달 목표</span>
            <strong>{getMonthLabel(currentMonth)}</strong>
            <p>과제, 시험, 팀 일정을 날짜별로 정리하세요.</p>
          </div>
          <div className="legend-card">
            <h2>일정 유형</h2>
            <ul>
              {scheduleTypes.map((type) => (
                <li key={type.value}>
                  <span className={`legend-dot ${type.className}`} />
                  <span>{type.label}</span>
                </li>
              ))}
            </ul>
          </div>
        </aside>

        <div className="calendar-main">
          <div className="calendar-card">
            <div className="calendar-card-header">
              <div>
                <h2>{getMonthLabel(currentMonth)}</h2>
                <p>{isLoading ? '일정을 불러오는 중입니다.' : '날짜를 눌러 상세 일정을 확인하세요.'}</p>
              </div>
              <div className="calendar-arrow-row">
                <button className="text-button" type="button" onClick={() => moveMonth(-1)}>
                  ‹
                </button>
                <button className="text-button" type="button" onClick={() => moveMonth(1)}>
                  ›
                </button>
              </div>
            </div>
            <div className="calendar-weekdays">
              {weekdays.map((weekday) => (
                <span key={weekday}>{weekday}</span>
              ))}
            </div>
            <div className="calendar-grid">
              {monthDays.map((date) => {
                const dateKey = getDateKey(date)
                const daySchedules = schedulesByDate[dateKey] ?? []
                const isCurrentMonth = date.getMonth() === currentMonth.getMonth()

                return (
                  <button
                    className={[
                      'calendar-day',
                      isCurrentMonth ? '' : 'muted-day',
                      dateKey === todayKey ? 'today' : '',
                    ]
                      .filter(Boolean)
                      .join(' ')}
                    key={dateKey}
                    type="button"
                    onClick={() => openDateModal(date)}
                  >
                    <span className="calendar-date-number">{date.getDate()}</span>
                    <span className="calendar-event-list">
                      {daySchedules.slice(0, 3).map((schedule) => {
                        const type = getType(schedule.type)

                        return (
                          <span
                            className={`calendar-event ${type.className}`}
                            key={schedule.id}
                          >
                            {schedule.title}
                          </span>
                        )
                      })}
                      {daySchedules.length > 3 && (
                        <span className="calendar-more">+{daySchedules.length - 3}개 더</span>
                      )}
                    </span>
                  </button>
                )
              })}
            </div>
          </div>

          <aside className="today-panel">
            <div className="today-panel-title">오늘의 핵심 일정</div>
            <div className="upcoming-card">
              <h2>다가오는 일정</h2>
              {(schedulesByDate[todayKey] ?? []).length === 0 ? (
                <p className="empty-state">등록된 일정이 없어요</p>
              ) : (
                <ul>
                  {(schedulesByDate[todayKey] ?? []).map((schedule) => (
                    <li key={schedule.id}>
                      <strong>{schedule.title}</strong>
                      <span>{formatTime(schedule.start_at)}</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </aside>
        </div>
      </section>

      {isModalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section className="modal-panel schedule-modal" aria-label="일정 확인 및 수정">
            <div className="modal-header">
              <div>
                <h2>일정 확인 및 수정</h2>
                <p>{formatDate(`${selectedDate}T00:00:00`)}</p>
              </div>
              <button className="text-button" type="button" onClick={closeModal}>
                닫기
              </button>
            </div>

            <div className="schedule-modal-grid">
              <div className="selected-schedules">
                <div className="selected-schedules-header">
                  <h3>선택한 날짜의 일정</h3>
                  <button
                    className="button secondary"
                    type="button"
                    onClick={() => resetFormForDate(selectedDate)}
                  >
                    새 일정
                  </button>
                </div>
                {selectedSchedules.length === 0 ? (
                  <p className="empty-state">현재 선택된 일정이 없습니다.</p>
                ) : (
                  <ul>
                    {selectedSchedules.map((schedule) => {
                      const type = getType(schedule.type)

                      return (
                        <li key={schedule.id}>
                          <button type="button" onClick={() => openEditForm(schedule)}>
                            <span className={`calendar-event ${type.className}`}>
                              {type.label}
                            </span>
                            <strong>{schedule.title}</strong>
                            <span>
                              {formatTime(schedule.start_at)} - {formatTime(schedule.end_at)}
                            </span>
                          </button>
                          <button
                            className="text-button danger-text"
                            type="button"
                            onClick={() => handleDelete(schedule)}
                          >
                            삭제
                          </button>
                        </li>
                      )
                    })}
                  </ul>
                )}
              </div>

              <form className="field-stack" onSubmit={handleSave}>
                <h3>{editingSchedule ? '일정 수정' : '일정 입력'}</h3>
                <label className="field">
                  일정명
                  <input
                    type="text"
                    value={formState.title}
                    onChange={(event) =>
                      setFormState((current) => ({
                        ...current,
                        title: event.target.value,
                      }))
                    }
                    placeholder="일정명을 입력하세요"
                    required
                  />
                </label>
                <label className="field">
                  일정 유형
                  <select
                    value={formState.type}
                    onChange={(event) =>
                      setFormState((current) => ({
                        ...current,
                        type: Number(event.target.value),
                      }))
                    }
                    required
                  >
                    {scheduleTypes.map((type) => (
                      <option key={type.value} value={type.value}>
                        {type.label}
                      </option>
                    ))}
                  </select>
                </label>
                <div className="inline-fields">
                  <label className="field">
                    시작 시간
                    <input
                      type="datetime-local"
                      value={formState.start_at}
                      onChange={(event) =>
                        setFormState((current) => ({
                          ...current,
                          start_at: event.target.value,
                        }))
                      }
                      required
                    />
                  </label>
                  <label className="field">
                    종료 시간
                    <input
                      type="datetime-local"
                      value={formState.end_at}
                      onChange={(event) =>
                        setFormState((current) => ({
                          ...current,
                          end_at: event.target.value,
                        }))
                      }
                      required
                    />
                  </label>
                </div>
                <label className="field">
                  메모
                  <textarea
                    value={formState.description}
                    onChange={(event) =>
                      setFormState((current) => ({
                        ...current,
                        description: event.target.value,
                      }))
                    }
                    placeholder="일정 관련 메모를 입력하세요"
                  />
                </label>
                <div className="button-row">
                  <button className="button" type="submit">
                    {editingSchedule ? '수정' : '저장'}
                  </button>
                  <button
                    className="button secondary"
                    type="button"
                    onClick={() => resetFormForDate(selectedDate)}
                  >
                    취소
                  </button>
                </div>
              </form>
            </div>
          </section>
        </div>
      )}
    </>
  )
}
