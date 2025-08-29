'use client'

import { useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { useAuth } from '@/contexts/AuthContext'
import ProtectedRoute from '@/components/ProtectedRoute'
import Navbar from '@/components/Navbar'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { ticketAPI, adminAPI } from '@/services/api'
import toast from 'react-hot-toast'
import { format } from 'date-fns'
import { 
  ChatBubbleLeftIcon,
  UserIcon,
  StarIcon
} from '@heroicons/react/24/outline'
import { StarIcon as StarIconSolid } from '@heroicons/react/24/solid'

export default function TicketDetail() {
  const params = useParams()
  const router = useRouter()
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const ticketId = parseInt(params.id as string)

  const [newComment, setNewComment] = useState('')
  const [rating, setRating] = useState(0)
  const [feedback, setFeedback] = useState('')
  const [showRating, setShowRating] = useState(false)

  const { data: ticket, isLoading, error } = useQuery(
    ['ticket', ticketId],
    async () => {
      console.log('Fetching ticket with ID:', ticketId)
      const response = await ticketAPI.getTicket(ticketId)
      console.log('Ticket response:', response)
      return response.data
    },
    { 
      enabled: !!ticketId && !isNaN(ticketId),
      retry: 1,
      onError: (error: any) => {
        console.error('Error fetching ticket:', error)
        console.error('Error details:', error.response?.data)
        
        // Don't show toast for 403 errors - we'll handle them in the UI
        if (error.response?.status !== 403) {
          toast.error(`Failed to load ticket: ${error.response?.data?.message || error.message}`)
        }
      }
    }
  )

  const { data: comments = [] } = useQuery(
    ['comments', ticketId],
    async () => {
      console.log('Fetching comments for ticket:', ticketId)
      const response = await ticketAPI.getComments(ticketId)
      console.log('Comments response:', response)
      return response.data
    },
    { 
      enabled: !!ticketId && !isNaN(ticketId),
      onError: (error: any) => {
        console.error('Error fetching comments:', error)
      }
    }
  )

  const { data: supportAgents = [] } = useQuery(
    'support-agents',
    async () => {
      const response = await adminAPI.getSupportAgents()
      return response.data
    },
    { enabled: user?.role === 'ADMIN' || user?.role === 'SUPPORT_AGENT' }
  )

  const addCommentMutation = useMutation(
    (content: string) => ticketAPI.addComment(ticketId, content),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['comments', ticketId])
        setNewComment('')
        toast.success('Comment added successfully')
      },
      onError: (error: any) => {
        toast.error(error.response?.data?.message || 'Failed to add comment')
      }
    }
  )

  const updateStatusMutation = useMutation(
    (status: string) => ticketAPI.updateTicketStatus(ticketId, status),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['ticket', ticketId])
        toast.success('Status updated successfully')
      },
      onError: (error: any) => {
        toast.error(error.response?.data?.message || 'Failed to update status')
      }
    }
  )

  const assignTicketMutation = useMutation(
    (assigneeId: number) => ticketAPI.assignTicket(ticketId, assigneeId),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['ticket', ticketId])
        toast.success('Ticket assigned successfully')
      },
      onError: (error: any) => {
        toast.error(error.response?.data?.message || 'Failed to assign ticket')
      }
    }
  )

  const rateTicketMutation = useMutation(
    () => ticketAPI.rateTicket(ticketId, rating, feedback),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['ticket', ticketId])
        setShowRating(false)
        setRating(0)
        setFeedback('')
        toast.success('Rating submitted successfully')
      },
      onError: (error: any) => {
        toast.error(error.response?.data?.message || 'Failed to submit rating')
      }
    }
  )

  const handleAddComment = (e: React.FormEvent) => {
    e.preventDefault()
    if (!newComment.trim()) return
    addCommentMutation.mutate(newComment)
  }

  const handleStatusChange = (status: string) => {
    updateStatusMutation.mutate(status)
  }

  const handleAssign = (assigneeId: number) => {
    assignTicketMutation.mutate(assigneeId)
  }

  const handleRating = (e: React.FormEvent) => {
    e.preventDefault()
    if (rating === 0) {
      toast.error('Please select a rating')
      return
    }
    rateTicketMutation.mutate()
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return 'bg-red-100 text-red-800'
      case 'IN_PROGRESS': return 'bg-yellow-100 text-yellow-800'
      case 'RESOLVED': return 'bg-green-100 text-green-800'
      case 'CLOSED': return 'bg-gray-100 text-gray-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'URGENT': return 'bg-red-100 text-red-800'
      case 'HIGH': return 'bg-orange-100 text-orange-800'
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800'
      case 'LOW': return 'bg-green-100 text-green-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const canModifyTicket = () => {
    if (!ticket || !user) return false
    if (user.role === 'ADMIN') return true
    if (user.role === 'SUPPORT_AGENT') return ticket.assignee?.id === user.id
    return ticket.creator.id === user.id
  }

  const canRateTicket = () => {
    if (!ticket || !user) return false
    return ticket.creator.id === user.id && 
           (ticket.status === 'RESOLVED' || ticket.status === 'CLOSED') &&
           !ticket.rating
  }

  if (isLoading) {
    return (
      <ProtectedRoute>
        <div className="min-h-screen bg-gray-50">
          <Navbar />
          <div className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
            <div className="px-4 py-6 sm:px-0">
              <div className="animate-pulse">
                <div className="h-8 bg-gray-200 rounded w-1/4 mb-4"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2 mb-8"></div>
                <div className="bg-white shadow rounded-lg p-6">
                  <div className="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
                  <div className="h-4 bg-gray-200 rounded w-1/2 mb-4"></div>
                  <div className="h-32 bg-gray-200 rounded"></div>
                </div>
              </div>
              <div className="mt-4 text-center text-sm text-gray-500">
                Loading ticket #{ticketId}...
              </div>
            </div>
          </div>
        </div>
      </ProtectedRoute>
    )
  }

  if (error) {
    const is403Error = error.response?.status === 403
    
    return (
      <ProtectedRoute>
        <div className="min-h-screen bg-gray-50">
          <Navbar />
          <div className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
            <div className="px-4 py-6 sm:px-0">
              <div className="text-center">
                {is403Error ? (
                  <>
                    <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
                      <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.5 0L4.268 19.5c-.77.833.192 2.5 1.732 2.5z" />
                      </svg>
                    </div>
                    <h3 className="mt-2 text-lg font-medium text-gray-900">Access Denied</h3>
                    {user?.role === 'SUPPORT_AGENT' ? (
                      <p className="mt-1 text-sm text-gray-500">
                        This ticket is not assigned to you. Please ask an admin to assign this ticket to you if you need to work on it.
                      </p>
                    ) : (
                      <p className="mt-1 text-sm text-gray-500">
                        You are not allowed to view this ticket. You can only view tickets that you have created.
                      </p>
                    )}
                    <div className="mt-6">
                      <button
                        onClick={() => router.push('/tickets')}
                        className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
                      >
                        View My Tickets
                      </button>
                    </div>
                  </>
                ) : (
                  <>
                    <h3 className="text-lg font-medium text-gray-900">Error Loading Ticket</h3>
                    <p className="mt-1 text-sm text-gray-500">
                      Failed to load ticket #{ticketId}. Please try again.
                    </p>
                    <div className="mt-4 space-x-2">
                      <button
                        onClick={() => window.location.reload()}
                        className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
                      >
                        Retry
                      </button>
                      <button
                        onClick={() => router.back()}
                        className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                      >
                        Go Back
                      </button>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      </ProtectedRoute>
    )
  }

  if (!ticket) {
    return (
      <ProtectedRoute>
        <div className="min-h-screen bg-gray-50">
          <Navbar />
          <div className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
            <div className="px-4 py-6 sm:px-0">
              <div className="text-center">
                <h3 className="text-lg font-medium text-gray-900">Ticket not found</h3>
                <p className="mt-1 text-sm text-gray-500">
                  The ticket you're looking for doesn't exist or you don't have access to it.
                </p>
                <button
                  onClick={() => router.back()}
                  className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
                >
                  Go Back
                </button>
              </div>
            </div>
          </div>
        </div>
      </ProtectedRoute>
    )
  }

  const ticketData = ticket

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        
        <div className="max-w-4xl mx-auto py-6 sm:px-6 lg:px-8">
          <div className="px-4 py-6 sm:px-0">
            {/* Header */}
            <div className="mb-8">
              <div className="flex items-center justify-between">
                <div>
                  <h1 className="text-2xl font-bold text-gray-900">
                    Ticket #{ticketData.id}
                  </h1>
                  <p className="mt-1 text-sm text-gray-600">
                    Created {format(new Date(ticketData.createdAt), 'PPP')}
                  </p>
                </div>
                <div className="flex items-center space-x-2">
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getPriorityColor(ticketData.priority)}`}>
                    {ticketData.priority}
                  </span>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(ticketData.status)}`}>
                    {ticketData.status.replace('_', ' ')}
                  </span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Main Content */}
              <div className="lg:col-span-2 space-y-6">
                {/* Ticket Details */}
                <div className="bg-white shadow rounded-lg p-6">
                  <h2 className="text-lg font-medium text-gray-900 mb-4">
                    {ticketData.subject}
                  </h2>
                  <div className="prose max-w-none">
                    <p className="text-gray-700 whitespace-pre-wrap">
                      {ticketData.description}
                    </p>
                  </div>
                </div>

                {/* Rating */}
                {ticketData.rating && (
                  <div className="bg-white shadow rounded-lg p-6">
                    <h3 className="text-lg font-medium text-gray-900 mb-4">Rating</h3>
                    <div className="flex items-center space-x-2 mb-2">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <StarIconSolid
                          key={star}
                          className={`h-5 w-5 ${star <= ticketData.rating ? 'text-yellow-400' : 'text-gray-300'}`}
                        />
                      ))}
                      <span className="text-sm text-gray-600">({ticketData.rating}/5)</span>
                    </div>
                    {ticketData.feedback && (
                      <p className="text-gray-700 mt-2">{ticketData.feedback}</p>
                    )}
                  </div>
                )}

                {/* Rate Ticket */}
                {canRateTicket() && !showRating && (
                  <div className="bg-white shadow rounded-lg p-6">
                    <button
                      onClick={() => setShowRating(true)}
                      className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700"
                    >
                      <StarIcon className="h-4 w-4 mr-2" />
                      Rate This Resolution
                    </button>
                  </div>
                )}

                {showRating && (
                  <div className="bg-white shadow rounded-lg p-6">
                    <h3 className="text-lg font-medium text-gray-900 mb-4">Rate This Resolution</h3>
                    <form onSubmit={handleRating}>
                      <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Rating
                        </label>
                        <div className="flex items-center space-x-1">
                          {[1, 2, 3, 4, 5].map((star) => (
                            <button
                              key={star}
                              type="button"
                              onClick={() => setRating(star)}
                              className="focus:outline-none"
                            >
                              <StarIcon
                                className={`h-6 w-6 ${star <= rating ? 'text-yellow-400 fill-current' : 'text-gray-300'}`}
                              />
                            </button>
                          ))}
                        </div>
                      </div>
                      <div className="mb-4">
                        <label htmlFor="feedback" className="block text-sm font-medium text-gray-700">
                          Feedback (Optional)
                        </label>
                        <textarea
                          id="feedback"
                          rows={3}
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                          placeholder="Share your experience..."
                          value={feedback}
                          onChange={(e) => setFeedback(e.target.value)}
                        />
                      </div>
                      <div className="flex space-x-3">
                        <button
                          type="submit"
                          disabled={rateTicketMutation.isLoading}
                          className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 disabled:opacity-50"
                        >
                          {rateTicketMutation.isLoading ? 'Submitting...' : 'Submit Rating'}
                        </button>
                        <button
                          type="button"
                          onClick={() => setShowRating(false)}
                          className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                  </div>
                )}

                {/* Comments */}
                <div className="bg-white shadow rounded-lg p-6">
                  <h3 className="text-lg font-medium text-gray-900 mb-4">
                    <ChatBubbleLeftIcon className="h-5 w-5 inline mr-2" />
                    Comments ({comments?.length || 0})
                  </h3>
                  
                  {/* Add Comment Form */}
                  <form onSubmit={handleAddComment} className="mb-6">
                    <textarea
                      rows={3}
                      className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                      placeholder="Add a comment..."
                      value={newComment}
                      onChange={(e) => setNewComment(e.target.value)}
                    />
                    <div className="mt-3 flex justify-end">
                      <button
                        type="submit"
                        disabled={addCommentMutation.isLoading || !newComment.trim()}
                        className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        {addCommentMutation.isLoading ? 'Adding...' : 'Add Comment'}
                      </button>
                    </div>
                  </form>

                  {/* Comments List */}
                  <div className="space-y-4">
                    {comments?.map((comment: any) => (
                      <div key={comment.id} className="border-l-4 border-primary-200 pl-4">
                        <div className="flex items-center space-x-2 mb-2">
                          <UserIcon className="h-4 w-4 text-gray-400" />
                          <span className="text-sm font-medium text-gray-900">
                            {comment.author.firstName} {comment.author.lastName}
                          </span>
                          <span className="text-xs text-gray-500">
                            {format(new Date(comment.createdAt), 'PPp')}
                          </span>
                        </div>
                        <p className="text-gray-700 whitespace-pre-wrap">{comment.content}</p>
                      </div>
                    ))}
                    {(!comments || comments.length === 0) && (
                      <p className="text-gray-500 text-sm">No comments yet.</p>
                    )}
                  </div>
                </div>
              </div>

              {/* Sidebar */}
              <div className="space-y-6">
                {/* Ticket Info */}
                <div className="bg-white shadow rounded-lg p-6">
                  <h3 className="text-lg font-medium text-gray-900 mb-4">Ticket Information</h3>
                  <dl className="space-y-3">
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Created by</dt>
                      <dd className="text-sm text-gray-900">
                        {ticketData.creator.firstName} {ticketData.creator.lastName}
                      </dd>
                    </div>
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Assigned to</dt>
                      <dd className="text-sm text-gray-900">
                        {ticketData.assignee ? 
                          `${ticketData.assignee.firstName} ${ticketData.assignee.lastName}` : 
                          'Unassigned'
                        }
                      </dd>
                    </div>
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Created</dt>
                      <dd className="text-sm text-gray-900">
                        {format(new Date(ticketData.createdAt), 'PPp')}
                      </dd>
                    </div>
                    {ticketData.updatedAt !== ticketData.createdAt && (
                      <div>
                        <dt className="text-sm font-medium text-gray-500">Last updated</dt>
                        <dd className="text-sm text-gray-900">
                          {format(new Date(ticketData.updatedAt), 'PPp')}
                        </dd>
                      </div>
                    )}
                    {ticketData.resolvedAt && (
                      <div>
                        <dt className="text-sm font-medium text-gray-500">Resolved</dt>
                        <dd className="text-sm text-gray-900">
                          {format(new Date(ticketData.resolvedAt), 'PPp')}
                        </dd>
                      </div>
                    )}
                  </dl>
                </div>

                {/* Actions */}
                {(canModifyTicket() || user?.role === 'ADMIN') && (
                  <div className="bg-white shadow rounded-lg p-6">
                    <h3 className="text-lg font-medium text-gray-900 mb-4">Actions</h3>
                    
                    {/* Status Actions */}
                    <div className="space-y-3">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Update Status
                        </label>
                        <div className="grid grid-cols-2 gap-2">
                          {['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].map((status) => (
                            <button
                              key={status}
                              onClick={() => handleStatusChange(status)}
                              disabled={ticketData.status === status || updateStatusMutation.isLoading}
                              className={`px-3 py-2 text-xs font-medium rounded-md border ${
                                ticketData.status === status
                                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                                  : 'bg-white text-gray-700 hover:bg-gray-50 border-gray-300'
                              }`}
                            >
                              {status.replace('_', ' ')}
                            </button>
                          ))}
                        </div>
                      </div>

                      {/* Assignment */}
                      {(user?.role === 'ADMIN' || user?.role === 'SUPPORT_AGENT') && (
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">
                            Assign to
                          </label>
                          <select
                            className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                            onChange={(e) => e.target.value && handleAssign(parseInt(e.target.value))}
                            value={ticketData.assignee?.id || ''}
                          >
                            <option value="">Unassigned</option>
                            {supportAgents?.map((agent: any) => (
                              <option key={agent.id} value={agent.id}>
                                {agent.firstName} {agent.lastName}
                              </option>
                            ))}
                          </select>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}
