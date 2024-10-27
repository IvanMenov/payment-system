import * as React from 'react';
import Box from '@mui/material/Box';
import Tab from '@mui/material/Tab';
import Typography from '@mui/material/Typography';
import NewTransaction from './NewTransaction';
import { useState } from 'react';
import { Button, Tabs } from '@mui/material';
import { Store } from 'react-notifications-component';
import { useHistory } from "react-router-dom"
import ListMerchantTransactions from './ListMerchantTransactions';

const TabsMerchant = (props) => {
  const history = useHistory();
  const [value, setValue] =useState(0);
  const [user, setUser] = useState(props.currentUser)
  const [ keycloakInstance ] =useState(props.keycloakInstance);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  const handleLogout = () => {
    setUser({});
    keycloakInstance.logout()
    Store.addNotification({
      message: "Successfully logged out!",
      type: "success",
      insert: "top",
      container: "top-right",
      animationIn: ["animate__animated", "animate__fadeIn"],
      animationOut: ["animate__animated", "animate__fadeOut"],
      dismiss: {
        duration: 3000,
        onScreen: true
      }
    });

  }

  function CustomTabPanel(props) {
    const { children, value, index, ...other } = props;


  
    return (
      <div
        role="tabpanel"
        hidden={value !== index}
        id={`simple-tabpanel-${index}`}
        aria-labelledby={`simple-tab-${index}`}
        {...other}
      >
        {value === index && (
          <Box sx={{ p: 3 }}>
            <Typography>{children}</Typography>
          </Box>
        )}
      </div>
    );
  }
  return (
    <Box sx={{ width: '100%' }}>
      <Box sx={{ borderBottom: 1, borderColor: 'divider', alignItems: 'center', justifyContent: 'center', display: 'flex'  }}>
        <Tabs value={value} onChange={handleChange} aria-label="basic tabs example">
          <Tab label="Create Transaction" />
          <Tab label="Get Transactions" />
          <Button variant="contained" type ='submit' onClick={handleLogout}>
            Logout
          </Button>

        </Tabs>
      </Box>
      <CustomTabPanel value={value} index={0}>
        <NewTransaction keycloakInstance={keycloakInstance} currentUser={user}></NewTransaction>
      </CustomTabPanel>
      <CustomTabPanel value={value} index={1}>
       <ListMerchantTransactions keycloakInstance={keycloakInstance} currentUser={user} comesFromAdmin={false}></ListMerchantTransactions>
      </CustomTabPanel>
    </Box>
  )
}
export default TabsMerchant