import axios from 'axios'
import Cookies from 'js-cookie'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = Cookies.get('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      Cookies.remove('token')
      Cookies.remove('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const authAPI = {
  login: (credentials: { username: string; password: string }) =>
    api.post('/auth/signin', credentials),
  register: (userData: {
    username: string
    email: string
    password: string
    firstName: string
    lastName: string
    role?: string
  }) => api.post('/auth/signup', userData),
}

export const ticketAPI = {
  getTickets: () => api.get('/tickets'),
  getTicket: (id: number) => api.get(`/tickets/${id}`),
  createTicket: (data: {
    subject: string
    description: string
    priority: string
  }) => api.post('/tickets', data),
  updateTicketStatus: (id: number, status: string) =>
    api.put(`/tickets/${id}/status`, { status }),
  assignTicket: (id: number, assigneeId: number) =>
    api.put(`/tickets/${id}/assign`, { assigneeId }),
  addComment: (id: number, content: string) =>
    api.post(`/tickets/${id}/comments`, { content }),
  getComments: (id: number) => api.get(`/tickets/${id}/comments`),
  rateTicket: (id: number, rating: number, feedback?: string) =>
    api.post(`/tickets/${id}/rate`, { rating, feedback }),
  searchTickets: (params: {
    status?: string
    priority?: string
    assigneeId?: number
    creatorId?: number
    search?: string
  }) => api.get('/tickets/search', { params }),
}

export const adminAPI = {
  getUsers: () => api.get('/admin/users'),
  createUser: (userData: {
    username: string
    email: string
    password: string
    firstName: string
    lastName: string
    role: string
  }) => api.post('/admin/users', userData),
  updateUser: (id: number, userData: any) => api.put(`/admin/users/${id}`, userData),
  deleteUser: (id: number) => api.delete(`/admin/users/${id}`),
  getSupportAgents: () => api.get('/admin/users/support-agents'),
  getAllTickets: () => api.get('/admin/tickets'),
  forceAssignTicket: (id: number, assigneeId: number) =>
    api.put(`/admin/tickets/${id}/force-assign`, { assigneeId }),
  forceUpdateTicketStatus: (id: number, status: string) =>
    api.put(`/admin/tickets/${id}/force-status`, { status }),
  deleteTicket: (id: number) => api.delete(`/admin/tickets/${id}`),
  getStats: () => api.get('/admin/stats'),
}

export default api
