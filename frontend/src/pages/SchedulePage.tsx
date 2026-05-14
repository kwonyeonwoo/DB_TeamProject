import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { getErrorMessage } from '../api/errors'
import { schedulesApi } from '../api/schedules'
import type { Schedule, SaveScheduleRequest } from '../api/schedules'

const scheduleTypes = [
  { value: 1, label: '과제' },
  { value: 2, label: '시험' },
  { value: 3, label: '스터디' },
  { value: 4, label: '회의' },
  { value: 5, label: '기타' },
]

function toDateTimeLocal(value: string) {
  return value.slice(0, 16)
}

function toIsoLikeLocal(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

function formatDateTime(value: string) {
  return new Date(value).toLocaleString()
}

function getTypeLabel(type: number) {
  return scheduleTypes.find((item) => item.value === type)?.label ?? '기타'
}

function createEmptyForm() {
  return {
    title: '',
    start_at: '',
    end_at: '',
    description: '',
    type: 1,
  }
}

export function SchedulePage() {
  const [schedules, setSchedules] = useState<Schedule[]>([])
  const [filterStart, setFilterStart] = useState('')
  const [filterEnd, setFilterEnd] = useState('')
  const [appliedFilterStart, setAppliedFilterStart] = useState('')
  const [appliedFilterEnd, setAppliedFilterEnd] = useState('')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingSchedule, setEditingSchedule] = useState<Schedule | null>(null)
  const [formState, setFormState] = useState<SaveScheduleRequest>(createEmptyForm)

  const filterParams = useMemo(
    () => ({
      start_at: appliedFilterStart ? toIsoLikeLocal(appliedFilterStart) : undefined,
      end_at: appliedFilterEnd ? toIsoLikeLocal(appliedFilterEnd) : undefined,
    }),
    [appliedFilterEnd, appliedFilterStart],
  )

  const loadSchedules = useCallback(async () => {
    setIsLoading(true)

    try {
      const response = await schedulesApi.listSchedules(filterParams)
      setSchedules(response.items)
      setErrorMessage(null)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    } finally {
      setIsLoading(false)
    }
  }, [filterParams])

  useEffect(() => {
    let isMounted = true

    schedulesApi
      .listSchedules(filterParams)
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
  }, [filterParams])

  const openCreateModal = () => {
    setEditingSchedule(null)
    setFormState(createEmptyForm())
    setSuccessMessage(null)
    setErrorMessage(null)
    setIsModalOpen(true)
  }

  const openEditModal = (schedule: Schedule) => {
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
    setIsModalOpen(true)
  }

  const closeModal = () => {
    setIsModalOpen(false)
    setEditingSchedule(null)
    setFormState(createEmptyForm())
  }

  const handleFilterSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsLoading(true)
    setAppliedFilterStart(filterStart)
    setAppliedFilterEnd(filterEnd)
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
        setSuccessMessage('일정이 수정되었습니다.')
      } else {
        await schedulesApi.createSchedule(request)
        setSuccessMessage('일정이 등록되었습니다.')
      }

      closeModal()
      await loadSchedules()
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
      setSuccessMessage('일정이 삭제되었습니다.')
      setErrorMessage(null)
      await loadSchedules()
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    }
  }

  return (
    <>
      <section className="page-header">
        <p className="eyebrow">일정</p>
        <h1 className="page-title">개인 일정</h1>
        <p className="page-description">
          개인 일정만 조회하고 관리합니다. 기간 필터는 문서 기준의 겹치는 일정
          조회 조건을 사용합니다.
        </p>
        <div className="button-row">
          <button className="button" type="button" onClick={openCreateModal}>
            일정 등록
          </button>
        </div>
      </section>

      <form className="toolbar" onSubmit={handleFilterSubmit}>
        <label className="field compact">
          시작 기준
          <input
            type="datetime-local"
            value={filterStart}
            onChange={(event) => setFilterStart(event.target.value)}
          />
        </label>
        <label className="field compact">
          종료 기준
          <input
            type="datetime-local"
            value={filterEnd}
            onChange={(event) => setFilterEnd(event.target.value)}
          />
        </label>
        <button className="button" type="submit">
          조회
        </button>
        <button
          className="button secondary"
          type="button"
          onClick={() => {
            setFilterStart('')
            setFilterEnd('')
            setAppliedFilterStart('')
            setAppliedFilterEnd('')
            setIsLoading(true)
          }}
        >
          초기화
        </button>
      </form>

      {errorMessage && <p className="form-error">{errorMessage}</p>}
      {successMessage && <p className="form-success">{successMessage}</p>}

      <section className="schedule-board">
        {isLoading && <p className="empty-state">일정을 불러오고 있습니다.</p>}
        {!isLoading && schedules.length === 0 && (
          <p className="empty-state">조회된 일정이 없습니다.</p>
        )}
        {schedules.map((schedule) => (
          <article className="schedule-item" key={schedule.id}>
            <div className="schedule-date">
              <span>{new Date(schedule.start_at).toLocaleDateString()}</span>
              <strong>{new Date(schedule.start_at).toLocaleTimeString()}</strong>
            </div>
            <div className="schedule-content">
              <div className="post-meta">
                <span className="badge">{getTypeLabel(schedule.type)}</span>
                <span>
                  {formatDateTime(schedule.start_at)} - {formatDateTime(schedule.end_at)}
                </span>
              </div>
              <h2>{schedule.title}</h2>
              <p>{schedule.description ?? '설명이 없습니다.'}</p>
              <div className="button-row">
                <button
                  className="button secondary"
                  type="button"
                  onClick={() => openEditModal(schedule)}
                >
                  수정
                </button>
                <button
                  className="button danger"
                  type="button"
                  onClick={() => handleDelete(schedule)}
                >
                  삭제
                </button>
              </div>
            </div>
          </article>
        ))}
      </section>

      {isModalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section className="modal-panel" aria-label="일정 입력">
            <div className="modal-header">
              <h2>{editingSchedule ? '일정 수정' : '일정 등록'}</h2>
              <button className="text-button" type="button" onClick={closeModal}>
                닫기
              </button>
            </div>
            <form className="field-stack" onSubmit={handleSave}>
              <label className="field">
                제목
                <input
                  type="text"
                  value={formState.title}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      title: event.target.value,
                    }))
                  }
                  required
                />
              </label>
              <label className="field">
                유형
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
              <label className="field">
                시작
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
                종료
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
              <label className="field">
                설명
                <textarea
                  value={formState.description}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      description: event.target.value,
                    }))
                  }
                  placeholder="일정 설명을 입력하세요"
                />
              </label>
              <div className="button-row">
                <button className="button" type="submit">
                  저장
                </button>
                <button className="button secondary" type="button" onClick={closeModal}>
                  취소
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </>
  )
}
