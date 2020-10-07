<template>
  <header class="header" v-bind:class="[headerClass, scrollClass, {'open':showMenuMobile}]">
    <div class="header__inner">
      <div class="header__logo"><router-link to="/"><img src="@/assets/images/logo.svg" alt=""></router-link></div>
      <div class="header__mobile">
        <a href="#" class="header__mobile__burger" v-bind:class="{'open':showMenuMobile}" @click.prevent="toggleMobileMenu">
          <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
            viewBox="0 0 36 36" style="enable-background:new 0 0 36 36;" xml:space="preserve">
            <rect x="1.2" y="9" style="fill:#FFFFFF;" width="33.5" height="2"/>
            <path style="fill:#FFFFFF;" d="M34.2,19h-6v-2h6V19z M24.2,19h-9v-2h9V19z M11.2,19h-9v-2h9V19z"/>
            <rect x="1.2" y="25" style="fill:#FFFFFF;" width="33.5" height="2"/>
          </svg>
        </a>
        <router-link to="/" class="header__mobile__logo--closed">
          <svg @click="hideMobileMenu" id="Logo-header_footer" data-name="Logo-header&amp;footer" xmlns="http://www.w3.org/2000/svg" width="24.135" height="28.048" viewBox="0 0 24.135 28.048">
            <path id="Path_1" data-name="Path 1" d="M5504.21,5372.853v-6.307h4.309v6.307h5.641v3.487h-5.641v11.125q0,1.175,1.1,1.175h3.956v3.525h-5.444a3.8,3.8,0,0,1-2.84-1.1,3.955,3.955,0,0,1-1.078-2.9v-11.83H5499v-3.487Z" transform="translate(-5499 -5364.117)" fill="#fff"/>
            <path id="Path_1-2" data-name="Path 1" d="M5504.21,5372.853v-6.307h4.309v6.307h5.641v3.487h-5.641v11.125q0,1.175,1.1,1.175h3.956v3.525h-5.444a3.8,3.8,0,0,1-2.84-1.1,3.955,3.955,0,0,1-1.078-2.9v-11.83H5499v-3.487Z" transform="translate(-5499 -5364.117)" fill="#fff"/>
            <path id="Path_3" data-name="Path 3" d="M5515.167,5365.112a3.418,3.418,0,0,1,2.473-.995,3.472,3.472,0,0,1,2.5.955,3.291,3.291,0,0,1,.994,2.487,3.316,3.316,0,0,1-1.774,3.011,3.6,3.6,0,0,1-1.721.43,3.43,3.43,0,0,1-1.72-.457,3.529,3.529,0,0,1-1.291-1.25,3.4,3.4,0,0,1,.538-4.181Zm.927,4.006a2.315,2.315,0,0,0,3.092,0,2.313,2.313,0,0,0,0-3.119,2.318,2.318,0,0,0-3.092,0,2.315,2.315,0,0,0,0,3.119Z" transform="translate(-5497 -5364.117)" fill="#fff"/>
          </svg>
        </router-link>
        <router-link to="/" class="header__mobile__logo header__mobile__logo--open">
          <svg @click="hideMobileMenu" xmlns="http://www.w3.org/2000/svg" width="96.917" height="35.883" viewBox="0 0 96.917 35.883">
            <g id="logo" transform="translate(-66 -33)">
              <path id="Path_1" data-name="Path 1"  d="M5504.21,5372.853v-6.307h4.309v6.307h5.641v3.487h-5.641v11.125q0,1.175,1.1,1.175h3.956v3.525h-5.444a3.8,3.8,0,0,1-2.84-1.1,3.955,3.955,0,0,1-1.078-2.9v-11.83H5499v-3.487Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_2" data-name="Path 2" d="M5517.451,5377.123a8.97,8.97,0,0,1,3.584-3.565,10.645,10.645,0,0,1,5.191-1.253,10.535,10.535,0,0,1,5.171,1.253,9,9,0,0,1,3.564,3.565,11.866,11.866,0,0,1,0,10.773,8.989,8.989,0,0,1-3.564,3.564,10.525,10.525,0,0,1-5.171,1.254,10.635,10.635,0,0,1-5.191-1.254,8.929,8.929,0,0,1-3.584-3.584,11.825,11.825,0,0,1,0-10.753Zm4.6,10.087a5.816,5.816,0,0,0,8.3,0,6.743,6.743,0,0,0,1.587-4.72,6.671,6.671,0,0,0-1.587-4.7,5.837,5.837,0,0,0-8.3.02,7.753,7.753,0,0,0,0,9.4Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_3" data-name="Path 3" d="M5515.167,5365.112a3.418,3.418,0,0,1,2.473-.995,3.472,3.472,0,0,1,2.5.955,3.291,3.291,0,0,1,.994,2.487,3.316,3.316,0,0,1-1.774,3.011,3.6,3.6,0,0,1-1.721.43,3.43,3.43,0,0,1-1.72-.457,3.529,3.529,0,0,1-1.291-1.25,3.4,3.4,0,0,1,.538-4.181Zm.927,4.006a2.315,2.315,0,0,0,3.092,0,2.313,2.313,0,0,0,0-3.119,2.318,2.318,0,0,0-3.092,0,2.315,2.315,0,0,0,0,3.119Z" transform="translate(-5415 -5331.117)" fill="#fff"/>
              <path id="Path_4" data-name="Path 4" d="M5537.659,5364.117l-1.223,5.99h-.8l.25-5.99Z" transform="translate(-5399 -5331.117)" fill="#fff"/>
              <path id="Path_5" data-name="Path 5" d="M5559.7,5364.117l-1.222,5.99h-.8l.249-5.99Zm3.219,0-1.222,5.99h-.824l.25-5.99Z" transform="translate(-5400 -5331.117)" fill="#fff"/>
              <path id="Path_6" data-name="Path 6" d="M5543.559,5372.853v2.82h.391a7.5,7.5,0,0,1,6.7-3.368,8.2,8.2,0,0,1,4.407,1.253,9.03,9.03,0,0,1,3.231,3.565,12.5,12.5,0,0,1,0,10.753,8.991,8.991,0,0,1-3.231,3.584,8.2,8.2,0,0,1-4.407,1.254,7.361,7.361,0,0,1-6.621-3.291h-.391V5400h-4.27v-27.147Zm10.028,14.4a7.856,7.856,0,0,0,0-9.519,5.864,5.864,0,0,0-8.364.02,7.726,7.726,0,0,0,0,9.48,5.865,5.865,0,0,0,8.364.019Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_7" data-name="Path 7" d="M5567.415,5392.165h-4.27v-19.312h4.27Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_8" data-name="Path 8" d="M5572.35,5377.123a8.973,8.973,0,0,1,3.584-3.565,10.645,10.645,0,0,1,5.191-1.253,10.528,10.528,0,0,1,5.17,1.253,8.994,8.994,0,0,1,3.565,3.565,11.866,11.866,0,0,1,0,10.773,8.986,8.986,0,0,1-3.565,3.564,10.518,10.518,0,0,1-5.17,1.254,10.635,10.635,0,0,1-5.191-1.254,8.932,8.932,0,0,1-3.584-3.584,11.825,11.825,0,0,1,0-10.753Zm4.6,10.087a5.816,5.816,0,0,0,8.3,0,6.743,6.743,0,0,0,1.587-4.72,6.672,6.672,0,0,0-1.587-4.7,5.837,5.837,0,0,0-8.3.02,7.755,7.755,0,0,0,0,9.4Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_1-2" data-name="Path 1" d="M5504.21,5372.853v-6.307h4.309v6.307h5.641v3.487h-5.641v11.125q0,1.175,1.1,1.175h3.956v3.525h-5.444a3.8,3.8,0,0,1-2.84-1.1,3.955,3.955,0,0,1-1.078-2.9v-11.83H5499v-3.487Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_2-2" data-name="Path 2" d="M5517.451,5377.123a8.97,8.97,0,0,1,3.584-3.565,10.645,10.645,0,0,1,5.191-1.253,10.535,10.535,0,0,1,5.171,1.253,9,9,0,0,1,3.564,3.565,11.866,11.866,0,0,1,0,10.773,8.989,8.989,0,0,1-3.564,3.564,10.525,10.525,0,0,1-5.171,1.254,10.635,10.635,0,0,1-5.191-1.254,8.929,8.929,0,0,1-3.584-3.584,11.825,11.825,0,0,1,0-10.753Zm4.6,10.087a5.816,5.816,0,0,0,8.3,0,6.743,6.743,0,0,0,1.587-4.72,6.671,6.671,0,0,0-1.587-4.7,5.837,5.837,0,0,0-8.3.02,7.753,7.753,0,0,0,0,9.4Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_3-2" data-name="Path 3" d="M5515.167,5365.112a3.418,3.418,0,0,1,2.473-.995,3.472,3.472,0,0,1,2.5.955,3.291,3.291,0,0,1,.994,2.487,3.316,3.316,0,0,1-1.774,3.011,3.6,3.6,0,0,1-1.721.43,3.43,3.43,0,0,1-1.72-.457,3.529,3.529,0,0,1-1.291-1.25,3.4,3.4,0,0,1,.538-4.181Zm.927,4.006a2.315,2.315,0,0,0,3.092,0,2.313,2.313,0,0,0,0-3.119,2.318,2.318,0,0,0-3.092,0,2.315,2.315,0,0,0,0,3.119Z" transform="translate(-5415 -5331.117)" fill="#fff"/>
              <path id="Path_4-2" data-name="Path 4" d="M5537.659,5364.117l-1.223,5.99h-.8l.25-5.99Z" transform="translate(-5399 -5331.117)" fill="#fff"/>
              <path id="Path_5-2" data-name="Path 5" d="M5559.7,5364.117l-1.222,5.99h-.8l.249-5.99Zm3.219,0-1.222,5.99h-.824l.25-5.99Z" transform="translate(-5400 -5331.117)" fill="#fff"/>
              <path id="Path_6-2" data-name="Path 6" d="M5543.559,5372.853v2.82h.391a7.5,7.5,0,0,1,6.7-3.368,8.2,8.2,0,0,1,4.407,1.253,9.03,9.03,0,0,1,3.231,3.565,12.5,12.5,0,0,1,0,10.753,8.991,8.991,0,0,1-3.231,3.584,8.2,8.2,0,0,1-4.407,1.254,7.361,7.361,0,0,1-6.621-3.291h-.391V5400h-4.27v-27.147Zm10.028,14.4a7.856,7.856,0,0,0,0-9.519,5.864,5.864,0,0,0-8.364.02,7.726,7.726,0,0,0,0,9.48,5.865,5.865,0,0,0,8.364.019Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_7-2" data-name="Path 7" d="M5567.415,5392.165h-4.27v-19.312h4.27Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_8-2" data-name="Path 8" d="M5572.35,5377.123a8.973,8.973,0,0,1,3.584-3.565,10.645,10.645,0,0,1,5.191-1.253,10.528,10.528,0,0,1,5.17,1.253,8.994,8.994,0,0,1,3.565,3.565,11.866,11.866,0,0,1,0,10.773,8.986,8.986,0,0,1-3.565,3.564,10.518,10.518,0,0,1-5.17,1.254,10.635,10.635,0,0,1-5.191-1.254,8.932,8.932,0,0,1-3.584-3.584,11.825,11.825,0,0,1,0-10.753Zm4.6,10.087a5.816,5.816,0,0,0,8.3,0,6.743,6.743,0,0,0,1.587-4.72,6.672,6.672,0,0,0-1.587-4.7,5.837,5.837,0,0,0-8.3.02,7.755,7.755,0,0,0,0,9.4Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
            </g>
          </svg>
        </router-link>
        <div class="header__mobile__right header__cart">
          <a href="#" class="header__mobile__cart" v-if="$route.meta.layout != 'dashboard'">
              <svg xmlns="http://www.w3.org/2000/svg" width="24.08" height="24.001" viewBox="0 0 24.08 24.001">
                <g id="Group_1973" data-name="Group 1973" transform="translate(-1684 -36)">
                  <g id="Group_1972" data-name="Group 1972" transform="translate(1005.503 -324.638)">
                    <path id="Path_8968" data-name="Path 8968" d="M693.77,368.612h-6.464a1,1,0,0,0,0,2h6.464a1,1,0,0,0,0-2Z" fill="#ffffff"/>
                    <path id="Path_8969" data-name="Path 8969" d="M701.841,368.651a1,1,0,0,0-1.229.7l-3.628,13.28H684.092l-3.629-13.28a1,1,0,1,0-1.93.527l3.83,14.017a1,1,0,0,0,.965.736h14.42a1,1,0,0,0,.965-.736l3.829-14.017A1,1,0,0,0,701.841,368.651Z" fill="#ffffff"/>
                    <path id="Path_8970" data-name="Path 8970" d="M683.553,370.615h0a1,1,0,0,0,1-1c0-.285.1-6.98,6-6.98,5.873,0,5.969,6.7,5.969,6.978a1,1,0,0,0,2,0c0-3.105-1.666-8.978-7.97-8.978-6.325,0-8,5.873-8,8.978A1,1,0,0,0,683.553,370.615Z" fill="#ffffff"/>
                  </g>
                </g>
              </svg><span>{{ cartCount() }}</span>
          </a>
          <div class="header__notif" v-if="$route.meta.layout == 'dashboard'">
            <svg xmlns="http://www.w3.org/2000/svg" width="24.31" height="25.977" viewBox="0 0 24.31 25.977">
              <g id="Group_1975" data-name="Group 1975" transform="translate(-669.008 -477.688)">
                <g id="Group_1974" data-name="Group 1974">
                  <path id="Path_8972" data-name="Path 8972" d="M690.591,494.182c-.247,0-1.488-.037-1.658-.757a5.909,5.909,0,0,1,.035-1.77,46.892,46.892,0,0,0,.242-4.918c0-4.294-3.156-7.068-8.041-7.072h-.011c-4.885,0-8.041,2.778-8.041,7.072a46.583,46.583,0,0,0,.242,4.918,5.943,5.943,0,0,1,.035,1.77c-.17.72-1.412.757-1.659.757-1.58,0-2.727,1.344-2.727,3.2a2.863,2.863,0,0,0,2.727,2.918H674.6a.982.982,0,0,0,0-1.965h-2.862a.9.9,0,0,1-.762-.953c0-.613.236-1.231.762-1.231,1.9,0,3.236-.85,3.571-2.272a7.175,7.175,0,0,0,.008-2.407,44.586,44.586,0,0,1-.232-4.731c0-3.765,3.14-5.106,6.081-5.107s6.082,1.342,6.082,5.107a44.489,44.489,0,0,1-.233,4.729,7.187,7.187,0,0,0,.009,2.41c.335,1.421,1.669,2.271,3.57,2.271.63,0,.762.669.762,1.231a.9.9,0,0,1-.762.953H687.73a.982.982,0,0,0,0,1.965h2.861a2.863,2.863,0,0,0,2.727-2.918C693.318,495.17,691.949,494.182,690.591,494.182Z" fill="#333"/>
                  <path id="Path_8973" data-name="Path 8973" d="M683.939,498.944a.982.982,0,0,0-.982.983,1.773,1.773,0,1,1-3.546,0,.983.983,0,1,0-1.965,0,3.738,3.738,0,1,0,7.476,0A.983.983,0,0,0,683.939,498.944Z" fill="#333"/>
                </g>
                <circle id="Ellipse_209" data-name="Ellipse 209" cx="4.896" cy="4.896" r="4.896" transform="translate(680.747 477.688)" fill="#333"/>
              </g>
            </svg>
          </div>
        </div>
        <a href="#" class="header__mobile__search" v-if="$route.meta.layout != 'dashboard'"><svg @click.prevent="showSearchModal = true" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path id="Path_8967" data-name="Path 8967" d="M781.8,258.8l-8.266-8.266a8.658,8.658,0,1,0-6.771,3.263h.07a.96.96,0,0,0-.008-1.92h-.007a6.746,6.746,0,1,1,4.934-2.2,1.554,1.554,0,0,0-.153,1.636l8.844,8.844A.96.96,0,1,0,781.8,258.8Z" transform="translate(-758.086 -236.44)" fill="#fff"/></svg></a>
      </div>
      <div class="header__menu" v-if="$route.meta.layout != 'dashboard'">
        <nav>
          <ul>
            <li>
              <a href="#" @click.prevent="showSubmenu1">Sell</a>
              <div class="header__mobile-submenu" v-if="showSubmenuSell">
                <ul>
                  <li><router-link to="/vendor-benefits"><span @click="toggleMobileMenu">Benefits for vendors</span></router-link></li>
                  <li><router-link to="/vendor-benefits"><span @click="toggleMobileMenu">BECOME A VENDOR</span></router-link></li>
                </ul>
              </div>
              <transition name="fade" mode="out-in">
                <div class="arrow-up arrow-up--white" v-if="showSubmenuSell"></div>
              </transition>
              <transition name="fade" mode="out-in">
              <div class="header__submenu" v-if="showSubmenuSell">
                <div class="header__submenu__wrapper">
                  <div class="header__submenu__block">
                    <div class="header__submenu__block__img-btn">
                      <svg xmlns="http://www.w3.org/2000/svg" width="112.573" height="48.014" viewBox="0 0 112.573 48.014">
                        <g id="Group_1287" data-name="Group 1287" transform="translate(191.202 164.66)">
                          <g id="Group_1287-2" data-name="Group 1287">
                            <path id="Path_2334" data-name="Path 2334" d="M-188.762-140.19l12.82,22.53a2,2,0,0,0,2.726.753,2,2,0,0,0,.754-.753l12.51-22h0c1.39-2.43,2.75-5.09,2.75-8.52,0-9.09-7.63-16.48-17-16.48s-17,7.39-17,16.48A15.324,15.324,0,0,0-188.762-140.19Zm14.56-20.47a12.76,12.76,0,0,1,13,12.48c0,2.22-.81,4.09-2.23,6.56l-10.77,18.96-11.08-19.51a11.6,11.6,0,0,1-1.92-6A12.76,12.76,0,0,1-174.2-160.66Z"/>
                            <path id="Path_2335" data-name="Path 2335" d="M-174.2-141.66a7,7,0,0,0,7-7,7,7,0,0,0-7-7,7,7,0,0,0-7,7A7,7,0,0,0-174.2-141.66Zm0-10a3,3,0,0,1,3,3,3,3,0,0,1-3,3,3,3,0,0,1-3-3A3,3,0,0,1-174.2-151.66Z"/>
                          </g>
                          <path id="Path_2336" data-name="Path 2336" d="M-95.784-118.924c11.86,0,17.155-1.67,17.155-5.455v-32.6c0-3.729-5.3-5.4-17.155-5.4s-17.154,1.669-17.154,5.4v32.662C-112.938-120.594-107.643-118.924-95.784-118.924Zm0-38.884a51.024,51.024,0,0,1,10.67.824,50.958,50.958,0,0,1-10.67.823,50.958,50.958,0,0,1-10.67-.823A51.024,51.024,0,0,1-95.784-157.808Zm-12.58,5.044a49.294,49.294,0,0,0,12.58,1.143,49.294,49.294,0,0,0,12.58-1.143v6.049c-.812.4-3.739,1.43-12.58,1.43s-11.768-1.029-12.58-1.43Zm0,10.887a49.34,49.34,0,0,0,12.58,1.144,49.34,49.34,0,0,0,12.58-1.144v6.05c-.812.4-3.739,1.429-12.58,1.429s-11.768-1.029-12.58-1.429Zm0,10.887a49.251,49.251,0,0,0,12.58,1.144A49.251,49.251,0,0,0-83.2-130.99v6.062c-.812.4-3.751,1.429-12.58,1.429s-11.768-1.029-12.58-1.429Z"/>
                          <g id="Group_1289" data-name="Group 1289">
                            <rect id="Rectangle_1175" data-name="Rectangle 1175" width="26.279" height="3.511" transform="translate(-150.893 -142.409)"/>
                            <g id="Group_1288" data-name="Group 1288">
                              <path id="Path_2337" data-name="Path 2337" d="M-133.919-129.168l-1.916-2.057,10.143-9.426-10.143-9.429,1.916-2.057,12.351,11.486Z"/>
                            </g>
                          </g>
                        </g>
                      </svg>
                      <p @click="showSubmenuSell = !showSubmenuSell"><router-link to="/vendor-benefits" class="btn btn--std btn--blue">BECOME A VENDOR</router-link></p>
                    </div>
                  </div>
                  <div class="header__submenu__block">
                    <h4 class="header__submenu__block__title">Benefits for vendors</h4>
                    <div class="header__submenu__block__text">
                      <ul>
                        <li>Easily upload, promote and trade your data assets</li>
                        <li>Increase your clients & profits</li>
                        <li>Turn your assets into services</li>
                      </ul>
                    </div>
                    <router-link class="header__submenu__block__link" to="/vendor-benefits">
                      <span @click="showSubmenuSell = !showSubmenuSell">LEARN MORE<svg xmlns="http://www.w3.org/2000/svg" width="16.414" height="9.622" viewBox="0 0 16.414 9.622"><path id="Path_2312" data-name="Path 2312" d="M473.524-7260.857l7.515,7.5,7.485-7.5" transform="translate(-472.818 7261.565)" fill="none" stroke="#6c6c6c" stroke-width="2"/></svg></span>
                    </router-link>
                  </div>
                </div>
              </div>
              </transition>
            </li>
            <li>
              <a href="#" @click.prevent="showSubmenu2">Buy</a>
              <div class="header__mobile-submenu" v-if="showSubmenuBuy">
                <ul>
                  <li><router-link to="/vendor-benefits"><span @click="toggleMobileMenu">Benefits for data users</span></router-link></li>
                  <li><router-link to="/catalogue"><span @click="toggleMobileMenu">Data catalogue</span></router-link></li>
                </ul>
              </div>
              <transition name="fade" mode="out-in">
              <div class="arrow-up arrow-up--white" v-if="showSubmenuBuy"></div>
              </transition>
              <transition name="fade" mode="out-in">
              <div class="header__submenu" v-if="showSubmenuBuy">
                <div class="header__submenu__wrapper">
                  <div class="header__submenu__block">
                    <div class="header__submenu__block__menu">
                      <ul>
                        <li @click="showSubmenuBuy = !showSubmenuBuy"><router-link to="/catalogue">Vector</router-link></li>
                        <li @click="showSubmenuBuy = !showSubmenuBuy"><router-link to="/catalogue">Title</router-link></li>
                        <li @click="showSubmenuBuy = !showSubmenuBuy"><router-link to="/catalogue">Raster</router-link></li>
                        <li @click="showSubmenuBuy = !showSubmenuBuy"><router-link to="/catalogue">APIs</router-link></li>
                        <li @click="showSubmenuBuy = !showSubmenuBuy"><router-link to="/catalogue">Bundles</router-link></li>
                      </ul>
                    </div>
                  </div>
                  <div class="header__submenu__block">
                    <h4 class="header__submenu__block__title">Benefits for vendors</h4>
                    <div class="header__submenu__block__text">
                      <ul>
                        <li>View, discover, compare and purchase geospatial data assets</li>
                        <li>Top-class secure payment, delivery and storage methods</li>
                        <li>0% fees</li>
                      </ul>
                    </div>
                    <router-link class="header__submenu__block__link" to="/user-benefits">
                      <span @click="showSubmenuBuy = !showSubmenuBuy">LEARN MORE<svg xmlns="http://www.w3.org/2000/svg" width="16.414" height="9.622" viewBox="0 0 16.414 9.622"><path id="Path_2312" data-name="Path 2312" d="M473.524-7260.857l7.515,7.5,7.485-7.5" transform="translate(-472.818 7261.565)" fill="none" stroke="#6c6c6c" stroke-width="2"/></svg></span>
                    </router-link>
                  </div>
                </div>
              </div>
              </transition>
            </li>
            <li @click="toggleMobileMenu"><router-link to="/about">About</router-link></li>
            <li @click="toggleMobileMenu"><router-link to="/faq">FAQ</router-link></li>
          </ul>
        </nav>
        <div class="header__menu__btn">
          <router-link to="/dashboard" class="btn" v-if="$store.getters.isAuthenticated"><span @click="toggleMobileMenu">Dashboard</span></router-link>
          <router-link to="/signin" class="btn" v-if="!$store.getters.isAuthenticated"><span @click="toggleMobileMenu">Login</span><span>/</span><span @click="toggleMobileMenu">Register</span></router-link>
        </div>
      </div>
      <div class="header__right">
        <div class="header__search" v-bind:class="{'open':showSearch}" v-if="$route.meta.layout != 'dashboard'">
          <input type="text" name="search" id="" placeholder="Search">
          <a href="#" @click.prevent="showSearch = !showSearch">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
              <path id="Path_8967" data-name="Path 8967" d="M781.8,258.8l-8.266-8.266a8.658,8.658,0,1,0-6.771,3.263h.07a.96.96,0,0,0-.008-1.92h-.007a6.746,6.746,0,1,1,4.934-2.2,1.554,1.554,0,0,0-.153,1.636l8.844,8.844A.96.96,0,1,0,781.8,258.8Z" transform="translate(-758.086 -236.44)" fill="#fff"/>
            </svg>
          </a>
        </div>
        <div class="header__notif" v-if="$route.meta.layout == 'dashboard'">
          <svg xmlns="http://www.w3.org/2000/svg" width="24.31" height="25.977" viewBox="0 0 24.31 25.977">
            <g id="Group_1975" data-name="Group 1975" transform="translate(-669.008 -477.688)">
              <g id="Group_1974" data-name="Group 1974">
                <path id="Path_8972" data-name="Path 8972" d="M690.591,494.182c-.247,0-1.488-.037-1.658-.757a5.909,5.909,0,0,1,.035-1.77,46.892,46.892,0,0,0,.242-4.918c0-4.294-3.156-7.068-8.041-7.072h-.011c-4.885,0-8.041,2.778-8.041,7.072a46.583,46.583,0,0,0,.242,4.918,5.943,5.943,0,0,1,.035,1.77c-.17.72-1.412.757-1.659.757-1.58,0-2.727,1.344-2.727,3.2a2.863,2.863,0,0,0,2.727,2.918H674.6a.982.982,0,0,0,0-1.965h-2.862a.9.9,0,0,1-.762-.953c0-.613.236-1.231.762-1.231,1.9,0,3.236-.85,3.571-2.272a7.175,7.175,0,0,0,.008-2.407,44.586,44.586,0,0,1-.232-4.731c0-3.765,3.14-5.106,6.081-5.107s6.082,1.342,6.082,5.107a44.489,44.489,0,0,1-.233,4.729,7.187,7.187,0,0,0,.009,2.41c.335,1.421,1.669,2.271,3.57,2.271.63,0,.762.669.762,1.231a.9.9,0,0,1-.762.953H687.73a.982.982,0,0,0,0,1.965h2.861a2.863,2.863,0,0,0,2.727-2.918C693.318,495.17,691.949,494.182,690.591,494.182Z" fill="#333"/>
                <path id="Path_8973" data-name="Path 8973" d="M683.939,498.944a.982.982,0,0,0-.982.983,1.773,1.773,0,1,1-3.546,0,.983.983,0,1,0-1.965,0,3.738,3.738,0,1,0,7.476,0A.983.983,0,0,0,683.939,498.944Z" fill="#333"/>
              </g>
              <circle id="Ellipse_209" data-name="Ellipse 209" cx="4.896" cy="4.896" r="4.896" transform="translate(680.747 477.688)" fill="#333"/>
            </g>
          </svg>
        </div>
        <div class="header__cart">
          <router-link to="/order-thankyou">
            <svg xmlns="http://www.w3.org/2000/svg" width="24.08" height="24.001" viewBox="0 0 24.08 24.001">
              <g id="Group_1973" data-name="Group 1973" transform="translate(-1684 -36)">
                <g id="Group_1972" data-name="Group 1972" transform="translate(1005.503 -324.638)">
                  <path id="Path_8968" data-name="Path 8968" d="M693.77,368.612h-6.464a1,1,0,0,0,0,2h6.464a1,1,0,0,0,0-2Z" fill="#ffffff"/>
                  <path id="Path_8969" data-name="Path 8969" d="M701.841,368.651a1,1,0,0,0-1.229.7l-3.628,13.28H684.092l-3.629-13.28a1,1,0,1,0-1.93.527l3.83,14.017a1,1,0,0,0,.965.736h14.42a1,1,0,0,0,.965-.736l3.829-14.017A1,1,0,0,0,701.841,368.651Z" fill="#ffffff"/>
                  <path id="Path_8970" data-name="Path 8970" d="M683.553,370.615h0a1,1,0,0,0,1-1c0-.285.1-6.98,6-6.98,5.873,0,5.969,6.7,5.969,6.978a1,1,0,0,0,2,0c0-3.105-1.666-8.978-7.97-8.978-6.325,0-8,5.873-8,8.978A1,1,0,0,0,683.553,370.615Z" fill="#ffffff"/>
                </g>
              </g>
            </svg><span>{{ cartCount() }}</span>
          </router-link>
        </div>
        <div class="header__login" v-if="$store.getters.isAuthenticated" @mouseover="showUserMenu=true" @mouseleave="showUserMenu=false">
          <div class="user_menu">
            <svg xmlns="http://www.w3.org/2000/svg" width="20.454" height="24" viewBox="0 0 20.454 24">
              <g id="Group_1976" data-name="Group 1976" transform="translate(-663.803 -610.487)">
                <path id="Path_8974" data-name="Path 8974" d="M674.03,621.6a5.557,5.557,0,1,0-5.557-5.556A5.563,5.563,0,0,0,674.03,621.6Zm0-9.125a3.568,3.568,0,1,1-3.568,3.569A3.572,3.572,0,0,1,674.03,612.476Z" fill="#fff"/>
                <path id="Path_8975" data-name="Path 8975" d="M674.03,622.607c-3.537,0-10.227,2.274-10.227,10.886v.994h20.454v-.994C684.257,624.881,677.568,622.607,674.03,622.607Zm-8.2,9.891c.572-7.809,7.879-7.9,8.2-7.9s7.63.093,8.2,7.9Z" fill="#fff"/>
              </g>
            </svg>
            <transition name="fade" mode="out-in">
            <div class="user_menu__dropdown" v-show="showUserMenu">
              <ul>
                <li><router-link to="/dashboard"><img src="@/assets/images/icons/dashboard/dashboard.svg" alt="">Dashboard</router-link></li>
                <li><router-link to="/dashboard/profile"><img src="@/assets/images/icons/dashboard/profile.svg" alt="">My profile</router-link></li>
                <li><router-link to="/dashboard/settings"><img src="@/assets/images/icons/dashboard/settings.svg" alt="">Settings</router-link></li>
                <li><a href="#" @click.prevent="logout"><img src="@/assets/images/icons/dashboard/logout.svg" alt="">Logout</a></li>
              </ul>
            </div>
            </transition>
          </div>
        </div>
        <div class="header__login" v-else>
          <router-link to="/signin">Login</router-link><span>/</span><router-link to="/login">Register</router-link>
        </div>
      </div>
    </div>
    <div class="modal-search" v-bind:class="{'open':showSearchModal}">
      <div class="modal-search__header">
        <a href="#" @click.prevent="showSearchModal = false" class="header__mobile__burger open">
          <svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px"
            viewBox="0 0 36 36" style="enable-background:new 0 0 36 36;" xml:space="preserve">
            <rect x="1.2" y="9" style="fill:#FFFFFF;" width="33.5" height="2"/>
            <path style="fill:#FFFFFF;" d="M34.2,19h-6v-2h6V19z M24.2,19h-9v-2h9V19z M11.2,19h-9v-2h9V19z"/>
            <rect x="1.2" y="25" style="fill:#FFFFFF;" width="33.5" height="2"/>
          </svg>
        </a>
        <router-link to="/" class="header__mobile__logo header__mobile__logo--open">
          <svg @click="showSearchModal = false" xmlns="http://www.w3.org/2000/svg" width="96.917" height="35.883" viewBox="0 0 96.917 35.883">
            <g id="logo" transform="translate(-66 -33)">
              <path id="Path_1" data-name="Path 1"  d="M5504.21,5372.853v-6.307h4.309v6.307h5.641v3.487h-5.641v11.125q0,1.175,1.1,1.175h3.956v3.525h-5.444a3.8,3.8,0,0,1-2.84-1.1,3.955,3.955,0,0,1-1.078-2.9v-11.83H5499v-3.487Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_2" data-name="Path 2" d="M5517.451,5377.123a8.97,8.97,0,0,1,3.584-3.565,10.645,10.645,0,0,1,5.191-1.253,10.535,10.535,0,0,1,5.171,1.253,9,9,0,0,1,3.564,3.565,11.866,11.866,0,0,1,0,10.773,8.989,8.989,0,0,1-3.564,3.564,10.525,10.525,0,0,1-5.171,1.254,10.635,10.635,0,0,1-5.191-1.254,8.929,8.929,0,0,1-3.584-3.584,11.825,11.825,0,0,1,0-10.753Zm4.6,10.087a5.816,5.816,0,0,0,8.3,0,6.743,6.743,0,0,0,1.587-4.72,6.671,6.671,0,0,0-1.587-4.7,5.837,5.837,0,0,0-8.3.02,7.753,7.753,0,0,0,0,9.4Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_3" data-name="Path 3" d="M5515.167,5365.112a3.418,3.418,0,0,1,2.473-.995,3.472,3.472,0,0,1,2.5.955,3.291,3.291,0,0,1,.994,2.487,3.316,3.316,0,0,1-1.774,3.011,3.6,3.6,0,0,1-1.721.43,3.43,3.43,0,0,1-1.72-.457,3.529,3.529,0,0,1-1.291-1.25,3.4,3.4,0,0,1,.538-4.181Zm.927,4.006a2.315,2.315,0,0,0,3.092,0,2.313,2.313,0,0,0,0-3.119,2.318,2.318,0,0,0-3.092,0,2.315,2.315,0,0,0,0,3.119Z" transform="translate(-5415 -5331.117)" fill="#fff"/>
              <path id="Path_4" data-name="Path 4" d="M5537.659,5364.117l-1.223,5.99h-.8l.25-5.99Z" transform="translate(-5399 -5331.117)" fill="#fff"/>
              <path id="Path_5" data-name="Path 5" d="M5559.7,5364.117l-1.222,5.99h-.8l.249-5.99Zm3.219,0-1.222,5.99h-.824l.25-5.99Z" transform="translate(-5400 -5331.117)" fill="#fff"/>
              <path id="Path_6" data-name="Path 6" d="M5543.559,5372.853v2.82h.391a7.5,7.5,0,0,1,6.7-3.368,8.2,8.2,0,0,1,4.407,1.253,9.03,9.03,0,0,1,3.231,3.565,12.5,12.5,0,0,1,0,10.753,8.991,8.991,0,0,1-3.231,3.584,8.2,8.2,0,0,1-4.407,1.254,7.361,7.361,0,0,1-6.621-3.291h-.391V5400h-4.27v-27.147Zm10.028,14.4a7.856,7.856,0,0,0,0-9.519,5.864,5.864,0,0,0-8.364.02,7.726,7.726,0,0,0,0,9.48,5.865,5.865,0,0,0,8.364.019Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_7" data-name="Path 7" d="M5567.415,5392.165h-4.27v-19.312h4.27Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_8" data-name="Path 8" d="M5572.35,5377.123a8.973,8.973,0,0,1,3.584-3.565,10.645,10.645,0,0,1,5.191-1.253,10.528,10.528,0,0,1,5.17,1.253,8.994,8.994,0,0,1,3.565,3.565,11.866,11.866,0,0,1,0,10.773,8.986,8.986,0,0,1-3.565,3.564,10.518,10.518,0,0,1-5.17,1.254,10.635,10.635,0,0,1-5.191-1.254,8.932,8.932,0,0,1-3.584-3.584,11.825,11.825,0,0,1,0-10.753Zm4.6,10.087a5.816,5.816,0,0,0,8.3,0,6.743,6.743,0,0,0,1.587-4.72,6.672,6.672,0,0,0-1.587-4.7,5.837,5.837,0,0,0-8.3.02,7.755,7.755,0,0,0,0,9.4Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_1-2" data-name="Path 1" d="M5504.21,5372.853v-6.307h4.309v6.307h5.641v3.487h-5.641v11.125q0,1.175,1.1,1.175h3.956v3.525h-5.444a3.8,3.8,0,0,1-2.84-1.1,3.955,3.955,0,0,1-1.078-2.9v-11.83H5499v-3.487Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_2-2" data-name="Path 2" d="M5517.451,5377.123a8.97,8.97,0,0,1,3.584-3.565,10.645,10.645,0,0,1,5.191-1.253,10.535,10.535,0,0,1,5.171,1.253,9,9,0,0,1,3.564,3.565,11.866,11.866,0,0,1,0,10.773,8.989,8.989,0,0,1-3.564,3.564,10.525,10.525,0,0,1-5.171,1.254,10.635,10.635,0,0,1-5.191-1.254,8.929,8.929,0,0,1-3.584-3.584,11.825,11.825,0,0,1,0-10.753Zm4.6,10.087a5.816,5.816,0,0,0,8.3,0,6.743,6.743,0,0,0,1.587-4.72,6.671,6.671,0,0,0-1.587-4.7,5.837,5.837,0,0,0-8.3.02,7.753,7.753,0,0,0,0,9.4Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_3-2" data-name="Path 3" d="M5515.167,5365.112a3.418,3.418,0,0,1,2.473-.995,3.472,3.472,0,0,1,2.5.955,3.291,3.291,0,0,1,.994,2.487,3.316,3.316,0,0,1-1.774,3.011,3.6,3.6,0,0,1-1.721.43,3.43,3.43,0,0,1-1.72-.457,3.529,3.529,0,0,1-1.291-1.25,3.4,3.4,0,0,1,.538-4.181Zm.927,4.006a2.315,2.315,0,0,0,3.092,0,2.313,2.313,0,0,0,0-3.119,2.318,2.318,0,0,0-3.092,0,2.315,2.315,0,0,0,0,3.119Z" transform="translate(-5415 -5331.117)" fill="#fff"/>
              <path id="Path_4-2" data-name="Path 4" d="M5537.659,5364.117l-1.223,5.99h-.8l.25-5.99Z" transform="translate(-5399 -5331.117)" fill="#fff"/>
              <path id="Path_5-2" data-name="Path 5" d="M5559.7,5364.117l-1.222,5.99h-.8l.249-5.99Zm3.219,0-1.222,5.99h-.824l.25-5.99Z" transform="translate(-5400 -5331.117)" fill="#fff"/>
              <path id="Path_6-2" data-name="Path 6" d="M5543.559,5372.853v2.82h.391a7.5,7.5,0,0,1,6.7-3.368,8.2,8.2,0,0,1,4.407,1.253,9.03,9.03,0,0,1,3.231,3.565,12.5,12.5,0,0,1,0,10.753,8.991,8.991,0,0,1-3.231,3.584,8.2,8.2,0,0,1-4.407,1.254,7.361,7.361,0,0,1-6.621-3.291h-.391V5400h-4.27v-27.147Zm10.028,14.4a7.856,7.856,0,0,0,0-9.519,5.864,5.864,0,0,0-8.364.02,7.726,7.726,0,0,0,0,9.48,5.865,5.865,0,0,0,8.364.019Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_7-2" data-name="Path 7" d="M5567.415,5392.165h-4.27v-19.312h4.27Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
              <path id="Path_8-2" data-name="Path 8" d="M5572.35,5377.123a8.973,8.973,0,0,1,3.584-3.565,10.645,10.645,0,0,1,5.191-1.253,10.528,10.528,0,0,1,5.17,1.253,8.994,8.994,0,0,1,3.565,3.565,11.866,11.866,0,0,1,0,10.773,8.986,8.986,0,0,1-3.565,3.564,10.518,10.518,0,0,1-5.17,1.254,10.635,10.635,0,0,1-5.191-1.254,8.932,8.932,0,0,1-3.584-3.584,11.825,11.825,0,0,1,0-10.753Zm4.6,10.087a5.816,5.816,0,0,0,8.3,0,6.743,6.743,0,0,0,1.587-4.72,6.672,6.672,0,0,0-1.587-4.7,5.837,5.837,0,0,0-8.3.02,7.755,7.755,0,0,0,0,9.4Z" transform="translate(-5433 -5331.117)" fill="#fff"/>
            </g>
          </svg>
        </router-link>
      </div>
      <div class="modal-search__main">
        <search></search>
      </div>
    </div>
  </header>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import store from '@/store';
import AccountApi from '@/service/account';
import Search from '@/components/Search.vue';
import {
  ServerResponse, LogoutResult,
} from '@/model';

@Component({
  components: { Search },
})
export default class Header extends Vue {
  @Prop({ default: 'header--nobg' }) private headerClass!: string;

  @Prop({ default: false }) private showMenuMobile!: boolean;

  scrollClass = '';

  showUserMenu = false;

  accountApi: AccountApi;

  showSubmenuSell = false;

  showSubmenuBuy = false;

  showSearch = false;

  showSearchModal = false;

  constructor() {
    super();

    this.accountApi = new AccountApi();
  }

  mounted(): void {
    window.addEventListener('scroll', this.handleScroll);
  }

  cartCount(): number {
    if (store.getters.getCart) {
      return store.getters.getCart.totalItems;
    }
    return 0;
  }

  destroyed(): void {
    window.removeEventListener('scroll', this.handleScroll);
  }

  handleScroll = (): void => {
    this.bgColor();
  }

  toggleMobileMenu():void {
    this.$emit('toggleMobileMenu', !this.showMenuMobile);
  }

  hideMobileMenu():void {
    this.$emit('toggleMobileMenu', false);
  }

  showSubmenu1():void {
    this.showSubmenuSell = !this.showSubmenuSell;
    if (this.showSubmenuBuy) {
      this.showSubmenuBuy = !this.showSubmenuBuy;
    }
  }

  showSubmenu2():void {
    this.showSubmenuBuy = !this.showSubmenuBuy;
    if (this.showSubmenuSell) {
      this.showSubmenuSell = !this.showSubmenuSell;
    }
  }

  bgColor():void {
    if (document.documentElement.scrollTop >= 50) {
      this.scrollClass = 'add_bg';
    } else {
      this.scrollClass = '';
    }
  }

  logout(): void {
    const router = this.$router;

    const name = 'Home';

    // Logout
    this.accountApi
      .logout()
      .then((logoutResponse: ServerResponse<LogoutResult>) => {
        if (logoutResponse.success) {
          // Set CSRF Token
          const {
            csrfToken: token,
            csrfHeader: header,
          } = logoutResponse.result;

          store.commit('setCsrfToken', { token, header });

          // Update user data
          store.commit('logout');

          if (router.currentRoute.name !== name) {
            router.push({ name });
          }
        }
      });
  }
}
</script>
