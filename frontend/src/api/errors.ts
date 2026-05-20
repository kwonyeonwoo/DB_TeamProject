import axios from 'axios'

interface ApiErrorBody {
  code?: string
  message?: string
  details?: unknown
}

export function getErrorMessage(error: unknown) {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    if (!error.response) {
      return '서버에 연결하지 못했습니다. 백엔드 실행 상태와 프론트 API 주소를 확인해 주세요.'
    }

    return error.response.data?.message ?? `요청을 처리하지 못했습니다. (HTTP ${error.response.status})`
  }

  if (error instanceof Error) {
    return error.message
  }

  return '알 수 없는 오류가 발생했습니다.'
}
