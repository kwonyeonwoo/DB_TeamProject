import { useEffect, useState } from 'react'
import { getErrorMessage } from '../api/errors'
import { reportReasonLabels, reportsApi } from '../api/reports'
import type { Report } from '../api/reports'

function getTargetLabel(report: Report) {
  return report.target_type === 'POST' ? '게시글' : '댓글'
}

function getStatusLabel(report: Report) {
  return report.status === 'PROCESSED' ? '처리 완료' : '대기'
}

export function AdminReportPage() {
  const [reports, setReports] = useState<Report[]>([])
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const loadReports = async () => {
    setIsLoading(true)

    try {
      const response = await reportsApi.listAdminReports()
      setReports(response.items)
      setErrorMessage(null)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    let isMounted = true

    reportsApi
      .listAdminReports()
      .then((response) => {
        if (!isMounted) {
          return
        }

        setReports(response.items)
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
  }, [])

  const handleProcess = async (report: Report) => {
    try {
      await reportsApi.processReport(report.id)
      setSuccessMessage(`신고 #${report.id}이 처리되었습니다.`)
      await loadReports()
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    }
  }

  return (
    <>
      {errorMessage && <p className="form-error">{errorMessage}</p>}
      {successMessage && <p className="form-success">{successMessage}</p>}

      <section className="post-list">
        {isLoading && <p className="empty-state">신고 목록을 불러오고 있습니다.</p>}
        {!isLoading && reports.length === 0 && (
          <p className="empty-state">접수된 신고가 없습니다.</p>
        )}
        {reports.map((report) => (
          <article className="post-item report-item" key={report.id}>
            <div>
              <div className="post-meta">
                <span className="badge">{getStatusLabel(report)}</span>
                <span>{getTargetLabel(report)}</span>
                <span>{new Date(report.created_at).toLocaleString()}</span>
              </div>
              <h2>신고 접수 #{report.id}</h2>
              <p>{report.target_display_name}</p>
              <div className="post-meta">
                <span>{reportReasonLabels[report.reason_type]}</span>
                {report.processed_at && (
                  <span>처리 시각 {new Date(report.processed_at).toLocaleString()}</span>
                )}
              </div>
            </div>
            <div className="report-actions">
              <button
                className="button"
                type="button"
                disabled={report.status === 'PROCESSED'}
                onClick={() => handleProcess(report)}
              >
                {report.status === 'PROCESSED' ? '처리 완료' : '처리'}
              </button>
            </div>
          </article>
        ))}
      </section>
    </>
  )
}
