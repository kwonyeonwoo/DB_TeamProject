import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { getErrorMessage } from '../api/errors'
import { groupsApi } from '../api/groups'
import type { Group, GroupDetailResponse } from '../api/groups'
import type { Schedule, SaveScheduleRequest } from '../api/schedules'
import { getScheduleTypeLabel, scheduleTypes } from '../constants/scheduleTypes'

function createEmptyScheduleForm(): SaveScheduleRequest {
  return {
    title: '',
    start_at: '',
    end_at: '',
    description: '',
    type: 4,
  }
}

function toDateTimeLocal(value: string) {
  return value.slice(0, 16)
}

function toIsoLikeLocal(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

function getTypeLabel(type: number) {
  return getScheduleTypeLabel(type)
}

export function GroupPage() {
  const [groups, setGroups] = useState<Group[]>([])
  const [selectedGroupId, setSelectedGroupId] = useState<number | null>(null)
  const [groupDetail, setGroupDetail] = useState<GroupDetailResponse | null>(null)
  const [groupSchedules, setGroupSchedules] = useState<Schedule[]>([])
  const [groupName, setGroupName] = useState('')
  const [joinCode, setJoinCode] = useState('')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [isScheduleModalOpen, setIsScheduleModalOpen] = useState(false)
  const [editingSchedule, setEditingSchedule] = useState<Schedule | null>(null)
  const [scheduleForm, setScheduleForm] =
    useState<SaveScheduleRequest>(createEmptyScheduleForm)

  const selectedGroup = useMemo(
    () => groups.find((group) => group.id === selectedGroupId) ?? null,
    [groups, selectedGroupId],
  )

  useEffect(() => {
    let isMounted = true

    groupsApi
      .listGroups()
      .then((response) => {
        if (!isMounted) {
          return
        }

        setGroups(response.items)
        setSelectedGroupId((currentId) => currentId ?? response.items[0]?.id ?? null)
        setErrorMessage(null)
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return
        }

        setErrorMessage(getErrorMessage(error))
      })

    return () => {
      isMounted = false
    }
  }, [])

  const loadGroupDetail = useCallback(async (groupId: number) => {
    const [detail, schedules] = await Promise.all([
      groupsApi.getGroup(groupId),
      groupsApi.listGroupSchedules(groupId),
    ])
    setGroupDetail(detail)
    setGroupSchedules(schedules.items)
  }, [])

  useEffect(() => {
    const handleProfileUpdated = () => {
      if (!selectedGroupId) {
        return
      }

      void loadGroupDetail(selectedGroupId)
        .then(() => setErrorMessage(null))
        .catch((error: unknown) => setErrorMessage(getErrorMessage(error)))
    }

    window.addEventListener('profile-updated', handleProfileUpdated)
    return () => {
      window.removeEventListener('profile-updated', handleProfileUpdated)
    }
  }, [loadGroupDetail, selectedGroupId])

  useEffect(() => {
    let isMounted = true

    if (!selectedGroupId) {
      return () => {
        isMounted = false
      }
    }

    Promise.all([
      groupsApi.getGroup(selectedGroupId),
      groupsApi.listGroupSchedules(selectedGroupId),
    ])
      .then(([detail, schedules]) => {
        if (!isMounted) {
          return
        }

        setGroupDetail(detail)
        setGroupSchedules(schedules.items)
        setErrorMessage(null)
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return
        }

        setErrorMessage(getErrorMessage(error))
      })

    return () => {
      isMounted = false
    }
  }, [selectedGroupId])

  const handleCreateGroup = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    try {
      const response = await groupsApi.createGroup(groupName)
      const groupList = await groupsApi.listGroups()
      setGroups(groupList.items)
      setSelectedGroupId(response.group.id)
      setGroupName('')
      setSuccessMessage(`그룹이 생성되었습니다. 코드: ${response.group.group_code}`)
      setErrorMessage(null)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    }
  }

  const handleJoinGroup = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    try {
      const membership = await groupsApi.joinGroup(joinCode)
      const groupList = await groupsApi.listGroups()
      setGroups(groupList.items)
      setSelectedGroupId(membership.group_id)
      setJoinCode('')
      setSuccessMessage('그룹에 참여했습니다.')
      setErrorMessage(null)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    }
  }

  const openCreateScheduleModal = () => {
    setEditingSchedule(null)
    setScheduleForm(createEmptyScheduleForm())
    setIsScheduleModalOpen(true)
  }

  const openEditScheduleModal = (schedule: Schedule) => {
    setEditingSchedule(schedule)
    setScheduleForm({
      title: schedule.title,
      start_at: toDateTimeLocal(schedule.start_at),
      end_at: toDateTimeLocal(schedule.end_at),
      description: schedule.description ?? '',
      type: schedule.type,
    })
    setIsScheduleModalOpen(true)
  }

  const closeScheduleModal = () => {
    setEditingSchedule(null)
    setScheduleForm(createEmptyScheduleForm())
    setIsScheduleModalOpen(false)
  }

  const handleSaveSchedule = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!selectedGroupId) {
      return
    }

    const request = {
      ...scheduleForm,
      start_at: toIsoLikeLocal(scheduleForm.start_at),
      end_at: toIsoLikeLocal(scheduleForm.end_at),
      type: Number(scheduleForm.type),
    }

    try {
      if (editingSchedule) {
        await groupsApi.updateGroupSchedule(selectedGroupId, editingSchedule.id, request)
        setSuccessMessage('그룹 일정이 수정되었습니다.')
      } else {
        await groupsApi.createGroupSchedule(selectedGroupId, request)
        setSuccessMessage('그룹 일정이 등록되었습니다.')
      }

      closeScheduleModal()
      await loadGroupDetail(selectedGroupId)
      setErrorMessage(null)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    }
  }

  const handleDeleteSchedule = async (schedule: Schedule) => {
    if (!selectedGroupId) {
      return
    }

    const confirmed = window.confirm('그룹 일정을 삭제할까요?')

    if (!confirmed) {
      return
    }

    try {
      await groupsApi.deleteGroupSchedule(selectedGroupId, schedule.id)
      setSuccessMessage('그룹 일정이 삭제되었습니다.')
      await loadGroupDetail(selectedGroupId)
      setErrorMessage(null)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    }
  }

  return (
    <>
      {errorMessage && <p className="form-error">{errorMessage}</p>}
      {successMessage && <p className="form-success">{successMessage}</p>}

      <section className="group-workspace">
        <aside className="group-sidebar">
          <form className="panel" onSubmit={handleCreateGroup}>
            <h2>그룹 만들기</h2>
            <label className="field">
              그룹명
              <input
                type="text"
                value={groupName}
                onChange={(event) => setGroupName(event.target.value)}
                placeholder="예: DB 스터디"
                required
              />
            </label>
            <div className="button-row">
              <button className="button" type="submit">
                만들기
              </button>
            </div>
          </form>

          <form className="panel" onSubmit={handleJoinGroup}>
            <h2>코드로 참여</h2>
            <label className="field">
              그룹 코드
              <input
                type="text"
                value={joinCode}
                onChange={(event) => setJoinCode(event.target.value)}
                placeholder="예: DB2026"
                required
              />
            </label>
            <div className="button-row">
              <button className="button secondary" type="submit">
                참여
              </button>
            </div>
          </form>

          <section className="panel">
            <h2>내 그룹</h2>
            <div className="group-list">
              {groups.length === 0 && <p className="muted">참여 중인 그룹이 없습니다.</p>}
              {groups.map((group) => (
                <button
                  className={
                    group.id === selectedGroupId ? 'group-list-item active' : 'group-list-item'
                  }
                  key={group.id}
                  type="button"
                  onClick={() => setSelectedGroupId(group.id)}
                >
                  <strong>{group.name}</strong>
                  <span>{group.group_code}</span>
                </button>
              ))}
            </div>
          </section>
        </aside>

        <section className="group-detail">
          {!selectedGroup && (
            <section className="panel">
              <p className="muted">그룹을 선택하거나 새 그룹을 만들어 주세요.</p>
            </section>
          )}

          {selectedGroup && groupDetail && (
            <>
              <section className="panel">
                <div className="group-heading">
                  <div>
                    <span className="badge">그룹 코드 {groupDetail.group.group_code}</span>
                    <h2>{groupDetail.group.name}</h2>
                  </div>
                  <button className="button" type="button" onClick={openCreateScheduleModal}>
                    그룹 일정 등록
                  </button>
                  <Link className="button secondary" to={`/schedule?group=${groupDetail.group.id}`}>
                    그룹 캘린더
                  </Link>
                </div>
                <div className="member-list">
                  {groupDetail.members.map((member) => (
                    <article className="member-item" key={`${member.group_id}-${member.user_id}`}>
                      <strong>{member.user_name ?? `사용자 ${member.user_id}`}</strong>
                      <span>{member.role === 'LEADER' ? '리더' : '멤버'}</span>
                    </article>
                  ))}
                </div>
              </section>

              <section className="schedule-board">
                {groupSchedules.length === 0 && (
                  <p className="empty-state">등록된 그룹 일정이 없습니다.</p>
                )}
                {groupSchedules.map((schedule) => (
                  <article className="schedule-item" key={schedule.id}>
                    <div className="schedule-date">
                      <span>{new Date(schedule.start_at).toLocaleDateString()}</span>
                      <strong>{new Date(schedule.start_at).toLocaleTimeString()}</strong>
                    </div>
                    <div className="schedule-content">
                      <div className="post-meta">
                        <span className="badge">{getTypeLabel(schedule.type)}</span>
                        <span>
                          {new Date(schedule.start_at).toLocaleString()} -{' '}
                          {new Date(schedule.end_at).toLocaleString()}
                        </span>
                      </div>
                      <h2>{schedule.title}</h2>
                      <p>{schedule.description ?? '설명이 없습니다.'}</p>
                      <div className="button-row">
                        <button
                          className="button secondary"
                          type="button"
                          onClick={() => openEditScheduleModal(schedule)}
                        >
                          수정
                        </button>
                        <button
                          className="button danger"
                          type="button"
                          onClick={() => handleDeleteSchedule(schedule)}
                        >
                          삭제
                        </button>
                      </div>
                    </div>
                  </article>
                ))}
              </section>
            </>
          )}
        </section>
      </section>

      {isScheduleModalOpen && (
        <div className="modal-backdrop" role="presentation">
          <section className="modal-panel" aria-label="그룹 일정 입력">
            <div className="modal-header">
              <h2>{editingSchedule ? '그룹 일정 수정' : '그룹 일정 등록'}</h2>
              <button className="text-button" type="button" onClick={closeScheduleModal}>
                닫기
              </button>
            </div>
            <form className="field-stack" onSubmit={handleSaveSchedule}>
              <label className="field">
                제목
                <input
                  type="text"
                  value={scheduleForm.title}
                  onChange={(event) =>
                    setScheduleForm((current) => ({
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
                  value={scheduleForm.type}
                  onChange={(event) =>
                    setScheduleForm((current) => ({
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
                  value={scheduleForm.start_at}
                  onChange={(event) =>
                    setScheduleForm((current) => ({
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
                  value={scheduleForm.end_at}
                  onChange={(event) =>
                    setScheduleForm((current) => ({
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
                  value={scheduleForm.description}
                  onChange={(event) =>
                    setScheduleForm((current) => ({
                      ...current,
                      description: event.target.value,
                    }))
                  }
                  placeholder="그룹 일정 설명을 입력하세요"
                />
              </label>
              <div className="button-row">
                <button className="button" type="submit">
                  저장
                </button>
                <button
                  className="button secondary"
                  type="button"
                  onClick={closeScheduleModal}
                >
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
