'use client'

import { useAuth } from '@/contexts/AuthContext'
import ProtectedRoute from '@/components/ProtectedRoute'
import Navbar from '@/components/Navbar'
import { useQuery } from 'react-query'
import { ticketAPI, adminAPI } from '@/services/api'
import { 
  TicketIcon, 
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  UserGroupIcon
} from '@heroicons/react/24/outline'
import Link from 'next/link'

interface DashboardStats {
  totalTickets: number
  openTickets: number
  inProgressTickets: number
  resolvedTickets: number
  myTickets?: number
}

export default function Dashboard() {
  const { user } = useAuth()
  
  const { data: tickets = [] } = useQuery('tickets', ticketAPI.getTickets)
  const { data: adminStats } = useQuery(
    'admin-stats', 
    adminAPI.getStats, 
    { enabled: user?.role === 'ADMIN' }
  )

  const getStats = (): DashboardStats => {
    if (user?.role === 'ADMIN' && adminStats?.data) {
      return {
        totalTickets: adminStats.data.tickets.total,
        openTickets: adminStats.data.tickets.open,
        inProgressTickets: adminStats.data.tickets.inProgress,
        resolvedTickets: adminStats.data.tickets.resolved,
      }
    }
    
    const userTickets = tickets.data || []
    return {
      totalTickets: userTickets.length,
      openTickets: userTickets.filter((t: any) => t.status === 'OPEN').length,
      inProgressTickets: userTickets.filter((t: any) => t.status === 'IN_PROGRESS').length,
      resolvedTickets: userTickets.filter((t: any) => t.status === 'RESOLVED').length,
      myTickets: userTickets.filter((t: any) => t.creator.id === user?.id).length,
    }
  }

  const stats = getStats()
  const recentTickets = (tickets.data || []).slice(0, 5)

  const statCards = [
    {
      name: 'Total Tickets',
      value: stats.totalTickets,
      icon: TicketIcon,
      color: 'bg-blue-500',
    },
    {
      name: 'Open',
      value: stats.openTickets,
      icon: ExclamationTriangleIcon,
      color: 'bg-red-500',
    },
    {
      name: 'In Progress',
      value: stats.inProgressTickets,
      icon: ClockIcon,
      color: 'bg-yellow-500',
    },
    {
      name: 'Resolved',
      value: stats.resolvedTickets,
      icon: CheckCircleIcon,
      color: 'bg-green-500',
    },
  ]

  if (user?.role === 'USER' && stats.myTickets !== undefined) {
    statCards.unshift({
      name: 'My Tickets',
      value: stats.myTickets,
      icon: UserGroupIcon,
      color: 'bg-purple-500',
    })
  }

  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        
        <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
          <div className="px-4 py-6 sm:px-0">
            <div className="mb-8">
              <h1 className="text-2xl font-bold text-gray-900">
                Welcome back, {user?.firstName}!
              </h1>
              <p className="mt-1 text-sm text-gray-600">
                Here's what's happening with your tickets today.
              </p>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4 mb-8">
              {statCards.map((card) => (
                <div key={card.name} className="bg-white overflow-hidden shadow rounded-lg">
                  <div className="p-5">
                    <div className="flex items-center">
                      <div className="flex-shrink-0">
                        <div className={`${card.color} rounded-md p-3`}>
                          <card.icon className="h-6 w-6 text-white" />
                        </div>
                      </div>
                      <div className="ml-5 w-0 flex-1">
                        <dl>
                          <dt className="text-sm font-medium text-gray-500 truncate">
                            {card.name}
                          </dt>
                          <dd className="text-lg font-medium text-gray-900">
                            {card.value}
                          </dd>
                        </dl>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Quick Actions */}
              <div className="bg-white shadow rounded-lg">
                <div className="px-4 py-5 sm:p-6">
                  <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
                    Quick Actions
                  </h3>
                  <div className="space-y-3">
                    <Link
                      href="/tickets/new"
                      className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                    >
                      Create New Ticket
                    </Link>
                    <Link
                      href="/tickets"
                      className="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                    >
                      View All Tickets
                    </Link>
                    {user?.role === 'ADMIN' && (
                      <Link
                        href="/admin"
                        className="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                      >
                        Admin Panel
                      </Link>
                    )}
                  </div>
                </div>
              </div>

              {/* Recent Tickets */}
              <div className="bg-white shadow rounded-lg">
                <div className="px-4 py-5 sm:p-6">
                  <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
                    Recent Tickets
                  </h3>
                  {recentTickets.length > 0 ? (
                    <div className="space-y-3">
                      {recentTickets.map((ticket: any) => (
                        <Link
                          key={ticket.id}
                          href={`/tickets/${ticket.id}`}
                          className="block p-3 border border-gray-200 rounded-lg hover:bg-gray-50"
                        >
                          <div className="flex items-center justify-between">
                            <div className="flex-1 min-w-0">
                              <p className="text-sm font-medium text-gray-900 truncate">
                                #{ticket.id} - {ticket.subject}
                              </p>
                              <p className="text-sm text-gray-500">
                                {ticket.priority} • {ticket.status}
                              </p>
                            </div>
                            <div className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                              ticket.status === 'OPEN' ? 'bg-red-100 text-red-800' :
                              ticket.status === 'IN_PROGRESS' ? 'bg-yellow-100 text-yellow-800' :
                              ticket.status === 'RESOLVED' ? 'bg-green-100 text-green-800' :
                              'bg-gray-100 text-gray-800'
                            }`}>
                              {ticket.status}
                            </div>
                          </div>
                        </Link>
                      ))}
                    </div>
                  ) : (
                    <p className="text-gray-500 text-sm">No tickets found</p>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ProtectedRoute>
  )
}
