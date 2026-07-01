import { UserManager, WebStorageStateStore } from 'oidc-client-ts'

const authority = (import.meta.env.VITE_OIDC_AUTHORITY as string | undefined) ?? 'http://localhost:8080'
const clientId = (import.meta.env.VITE_OIDC_CLIENT_ID as string | undefined) ?? 'maxigram-spa'

/**
 * OIDC Authorization Code + PKCE client against the monolith's Authorization Server.
 * Public clients get no refresh token (SAS design), so tokens are renewed silently via a
 * hidden iframe (prompt=none) while the AS session cookie is alive; when the session is
 * gone the renew times out and the 401 handler falls back to a full interactive login.
 */
export const userManager = new UserManager({
  authority,
  client_id: clientId,
  redirect_uri: `${window.location.origin}/callback`,
  post_logout_redirect_uri: window.location.origin,
  response_type: 'code',
  scope: 'openid profile email',
  userStore: new WebStorageStateStore({ store: window.localStorage }),
  automaticSilentRenew: true,
  silent_redirect_uri: `${window.location.origin}/silent-renew.html`,
  silentRequestTimeoutInSeconds: 5,
})
