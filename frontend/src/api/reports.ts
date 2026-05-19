import { apiClient, useMockApi } from './client'

export type ReportTargetType = 'POST' | 'COMMENT'
export type ReportStatus = 'PENDING' | 'PROCESSED'

export interface Report {
  id: number
  reporter_id: number
  target_type: ReportTargetType
  target_id: number
  target_display_name: string
  reason_type: number
  created_at: string
  status: ReportStatus
  processed_by: number | null
  processed_at: string | null
}

export interface CreateReportRequest {
  target_type: ReportTargetType
  target_id: number
  reason_type: number
}

export interface ReportListResponse {
  items: Report[]
}

const currentUserId = 2
const adminUserId = 1
const mockReportKey = 'db-teamproject:mock-reports'

const seedReports: Report[] = [
  {
    id: 1,
    reporter_id: 3,
    target_type: 'POST',
    target_id: 1,
    target_display_name: '데이터베이스 정규화 요약 자료',
    reason_type: 1,
    created_at: '2026-05-12T11:00:00',
    status: 'PENDING',
    processed_by: null,
    processed_at: null,
  },
  {
    id: 2,
    reporter_id: 4,
    target_type: 'COMMENT',
    target_id: 2,
    target_display_name: 'BCNF 분해 예제도 추가되면 좋겠습니다.',
    reason_type: 2,
    created_at: '2026-05-12T12:30:00',
    status: 'PENDING',
    processed_by: null,
    processed_at: null,
  },
]

export const reportReasonLabels: Record<number, string> = {
  1: '부적절한 내용',
  2: '광고/도배',
  3: '저작권 침해',
  4: '기타',
}

function readMockReports() {
  const rawReports = localStorage.getItem(mockReportKey)

  if (!rawReports) {
    localStorage.setItem(mockReportKey, JSON.stringify(seedReports))
    return seedReports
  }

  return JSON.parse(rawReports) as Report[]
}

function writeMockReports(reports: Report[]) {
  localStorage.setItem(mockReportKey, JSON.stringify(reports))
}

async function waitMockResponse() {
  await new Promise((resolve) => {
    window.setTimeout(resolve, 180)
  })
}

function createDisplayName(request: CreateReportRequest) {
  return request.target_type === 'POST'
    ? `게시글 #${request.target_id}`
    : `댓글 #${request.target_id}`
}

export const reportsApi = {
  async createReport(request: CreateReportRequest) {
    if (useMockApi) {
      await waitMockResponse()

      if (!['POST', 'COMMENT'].includes(request.target_type)) {
        throw new Error('신고 대상이 올바르지 않습니다.')
      }

      if (request.reason_type < 1 || request.reason_type > 4) {
        throw new Error('신고 사유를 선택해 주세요.')
      }

      const reports = readMockReports()
      const alreadyReported = reports.some(
        (report) =>
          report.reporter_id === currentUserId &&
          report.target_type === request.target_type &&
          report.target_id === request.target_id,
      )

      if (alreadyReported) {
        throw new Error('이미 신고한 대상입니다.')
      }

      const report: Report = {
        id: Math.max(0, ...reports.map((item) => item.id)) + 1,
        reporter_id: currentUserId,
        target_type: request.target_type,
        target_id: request.target_id,
        target_display_name: createDisplayName(request),
        reason_type: request.reason_type,
        created_at: new Date().toISOString(),
        status: 'PENDING',
        processed_by: null,
        processed_at: null,
      }

      writeMockReports([report, ...reports])
      return report
    }

    const response = await apiClient.post<Report>('/reports', request)
    return response.data
  },

  async listAdminReports() {
    if (useMockApi) {
      await waitMockResponse()
      return {
        items: readMockReports().sort(
          (left, right) =>
            new Date(right.created_at).getTime() -
            new Date(left.created_at).getTime(),
        ),
      } satisfies ReportListResponse
    }

    const response = await apiClient.get<ReportListResponse>('/admin/reports')
    return response.data
  },

  async processReport(reportId: number) {
    if (useMockApi) {
      await waitMockResponse()
      const reports = readMockReports()
      const report = reports.find((item) => item.id === reportId)

      if (!report) {
        throw new Error('신고를 찾을 수 없습니다.')
      }

      const nextReport: Report = {
        ...report,
        status: 'PROCESSED',
        processed_by: adminUserId,
        processed_at: new Date().toISOString(),
      }
      writeMockReports(reports.map((item) => (item.id === reportId ? nextReport : item)))
      return nextReport
    }

    const response = await apiClient.patch<Report>(`/admin/reports/${reportId}`, {
      status: 'PROCESSED',
    })
    return response.data
  },
}
