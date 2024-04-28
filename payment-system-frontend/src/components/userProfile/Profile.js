import React from 'react'
import { useState } from 'react';
import TabsMerchant from './TabsMerchant';
import ListMerchantsFromAdmin from './ListMerchantsFromAdmin';

const Profile = (props) => {
     const { children, value, index, ...other } = props;
     const [user, setUser] = useState(JSON.parse(props.currentUser))
     const [authenticated, setAuthenticated] = useState(props.authenticated)

     return (
          <div className="login-container">
               {user.principalType === 'MERCHANT' ?
                    <TabsMerchant authenticated={authenticated} currentUser={user}>
                    </TabsMerchant>
                    :
                    <ListMerchantsFromAdmin authenticated={authenticated} currentUser={user} ></ListMerchantsFromAdmin>
               }


          </div>
     )

}

export default Profile
