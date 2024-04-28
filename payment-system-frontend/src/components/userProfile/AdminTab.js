import { Button } from '@mui/material';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import { ACCESS_TOKEN } from '../../constants';
import { Store } from 'react-notifications-component';
import { useState } from 'react';
import { useHistory } from "react-router-dom"

const AdminTab = (props) => {
    const history = useHistory();
    const [user, setUser] = useState(props.currentUser)

    const handleLogout = () => {
        localStorage.removeItem(ACCESS_TOKEN);
        setUser({});
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
        history.push('/login');
        history.go();
    }

    return (
        <>
            <Box
                my={2}
                display="flex"
                alignItems="center"
                gap={2}
                p={2}
            >
                <Button variant="contained" type='submit' onClick={handleLogout}>
                    Logout
                </Button>
                <TextField
                    value={user.email}
                    id="outlined-read-only-input"
                    label="User"
                    InputProps={{
                        min: 0,
                        readOnly: true,
                        style: { textAlign: 'center' }
                    }}

                />
            </Box></>

    );
}
export default AdminTab