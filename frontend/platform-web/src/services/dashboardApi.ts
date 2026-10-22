
export interface DashboardFeature {
    code: string
    displayName: string
    description: string | null
    enabled: boolean
}

export interface DashboardResponse {
    userSubject: string
    features: DashboardFeature[]
}

export interface BasicCredentials {
    username: string
    password: string
}

export async function fetchDashboard(
    credentials: BasicCredentials,
): Promise<DashboardResponse> {
    const authorization = `Basic ${btoa(
        `${credentials.username}:${credentials.password}`,
    )}`

    const response = await fetch('/api/platform/dashboard', {
        method: 'GET',
        headers: {
            Accept: 'application/json',
            Authorization: authorization,
        },
    })

    if (response.status === 401) {
        throw new Error('Authentication failed. Check your username and password.')
    }

    if (response.status === 403) {
        throw new Error('You are not authorized to access the dashboard.')
    }

    if (!response.ok) {
        throw new Error(`Dashboard request failed (HTTP ${response.status}).`)
    }

    return (await response.json()) as DashboardResponse
}
