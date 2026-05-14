import axios from 'axios'

interface ApiErrorBody {
  code?: string
  message?: string
  details?: unknown
}

export function getErrorMessage(error: unknown) {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    return error.response?.data?.message ?? '요청을 처리하지 못했습니다.'
  }

  if (error instanceof Error) {
    return error.message
  }

  return '알 수 없는 오류가 발생했습니다.'
}
