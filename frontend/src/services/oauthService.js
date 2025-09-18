import { CONFIG } from "../config/constants";

export const googleOAuth2 = {
    redirectToBackend: () => {
        window.location.href = `${CONFIG.API.BASE_URL}/oauth2/authorization/google`;
    }
};

