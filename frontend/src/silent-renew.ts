// Loaded in the hidden silent-renew iframe: completes the prompt=none code exchange
// and hands the fresh tokens back to the opener's UserManager.
import { userManager } from './auth/oidc'

void userManager.signinSilentCallback()
