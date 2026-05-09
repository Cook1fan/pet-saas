export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = any> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export interface PageQuery {
  page: number
  pageSize: number
  [key: string]: any
}
