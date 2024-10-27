import React from 'react'
import TabsMerchant from './TabsMerchant';
import ListMerchantsFromAdmin from './ListMerchantsFromAdmin';
import { useState } from 'react';

const Profile = (props) => {
     const [ currentPrincipal ] = useState(props.principal);
     const [keycloakInstance] = useState( props.keycloakInstance)

     return (
          <div className="login-container">
               {currentPrincipal.principalType === 'MERCHANT' ?
                    <TabsMerchant keycloakInstance={keycloakInstance} currentUser={currentPrincipal}>
                    </TabsMerchant>
                    :
                    <ListMerchantsFromAdmin keycloakInstance={keycloakInstance} currentUser={currentPrincipal} ></ListMerchantsFromAdmin>
               }


          </div>
     )

}

export default Profile
