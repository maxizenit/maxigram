import { UserManager, WebStorageStateStore } from 'oidc-client-ts'

const authority = (import.meta.env.VITE_OIDC_AUTHORITY as string | undefined) ?? 'http://localhost:8080'
const clientId = (import.meta.env.VITE_OIDC_CLIENT_ID as string | undefined) ?? 'maxigram-spa'

/** OIDC Authorization Code + PKCE client against the monolith's Authorization Server. */
export const userManager = new UserManager({
  authority,
  client_id: clientId,
  redirect_uri: `${window.location.origin}/callback`,
  post_logout_redirect_uri: window.location.origin,
  response_type: 'code',
  scope: 'openid profile email',
  userStore: new WebStorageStateStore({ store: window.localStorage }),
})
