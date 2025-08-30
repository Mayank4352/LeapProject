'use client'

import { useAuth } from '../contexts/AuthContext'
import { useRouter } from 'next/navigation'
import { useEffect } from 'react'

interface ProtectedRouteProps {
  children: React.ReactNode
  requiredRole?: 'USER' | 'SUPPORT_AGENT' | 'ADMIN'
}

type Role = 'USER' | 'SUPPORT_AGENT' | 'ADMIN'

export default function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
  const { user, loading } = useAuth()
  const router = useRouter()

  useEffect(() => {
    if (!loading) {
      if (!user) {
        router.push('/login')
        return
      }

      if (requiredRole && user.role) {
        const roleHierarchy: Record<Role, number> = { 
          USER: 0, 
          SUPPORT_AGENT: 1, 
          ADMIN: 2 
        }
        const userLevel = roleHierarchy[user.role as Role]
        const requiredLevel = roleHierarchy[requiredRole]

        if (userLevel < requiredLevel) {
          router.push('/dashboard')
          return
        }
      }
    }
  }, [user, loading, router, requiredRole])

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-500"></div>
      </div>
    )
  }

  if (!user) {
    return null
  }

  if (requiredRole && user.role) {
    const roleHierarchy: Record<Role, number> = { 
      USER: 0, 
      SUPPORT_AGENT: 1, 
      ADMIN: 2 
    }
    const userLevel = roleHierarchy[user.role as Role]
    const requiredLevel = roleHierarchy[requiredRole]

    if (userLevel < requiredLevel) {
      return null
    }
  }

  return <>{children}</>
}
