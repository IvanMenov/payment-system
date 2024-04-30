import * as React from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Paper from '@mui/material/Paper';
import { useState, useEffect } from 'react';
import { getMerchant, parseReadableStreamToJson } from '../../APIUtils';
import { Store } from 'react-notifications-component';
import { Button } from '@mui/material';


const ListMerchantTransactions = (props) => {
    const [user, setUser] = useState(props.currentUser)
    const [rows, setRows] = useState([])
    const [comesFromAdmin, setComesFromAdmin] = useState(props.comesFromAdmin)


    useEffect(() => {
        updateTransactions()
    }, []);

    async function updateTransactions() {
        try {
            const updatePrincipal = await getMerchant(user.id)
            if (updatePrincipal != null && updatePrincipal.transactionList != null) {
                let currentRowsList = []
                let list = updatePrincipal.transactionList
                for (let i = 0; i < list.length; i++) {
                    currentRowsList.push(list[i]);
                }
                let sortedByTimestamp = currentRowsList.sort((a, b) => b.timestamp - a.timestamp)
                setRows(sortedByTimestamp)
                setUser(updatePrincipal)
                Store.addNotification({
                    message: "Successfully loaded transaction!",
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

        } catch (error) {
            const errorResponse = await parseReadableStreamToJson(error.body)
            Store.addNotification({
                message: errorResponse,
                type: "danger",
                insert: "top",
                container: "top-right",
                animationIn: ["animate__animated", "animate__fadeIn"],
                animationOut: ["animate__animated", "animate__fadeOut"],
                dismiss: {
                    duration: 3000,
                    onScreen: true
                }
            });
        };

    }
    return (
        <Box
            component="form"
            sx={{
                '& .MuiTextField-root': { m: 1, width: '25ch' },
            }}
            noValidate
            autoComplete="off"
        >
            <Box
                gap={2}
                p={2}>
                {
                    comesFromAdmin === true ?
                        <>
                            <Button sx={{marginTop :'10px'}}
                            variant="contained" type='submit' >
                                Back
                            </Button>
                        </>
                        : <></>

                }

                <TextField
                    value={user.email}
                    id="outlined-read-only-input"
                    label="Merchant email"
                    InputProps={{
                        min: 0,
                        readOnly: true,
                        style: { textAlign: 'center' }
                    }}

                />
                <TextField
                    value={user.totalTransactionSum}
                    id="outlined-read-only-input"
                    label="Total Transaction Sum"
                    InputProps={{
                        min: 0,
                        readOnly: true,
                        style: { textAlign: 'center' }
                    }}

                />

            </Box>

            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 650 }} aria-label="simple table">
                    <TableHead>
                        <TableRow sx={{ border: 2 }}>
                            <TableCell sx={{ border: 2 }}>Transaction UUID</TableCell>
                            <TableCell sx={{ border: 2 }} >Amount</TableCell>
                            <TableCell sx={{ border: 2 }}>Customer Email</TableCell>
                            <TableCell sx={{ border: 2 }}>customer Phone</TableCell>
                            <TableCell sx={{ border: 2 }}>Reference UUID</TableCell>
                            <TableCell sx={{ border: 2 }}>Status</TableCell>
                            <TableCell sx={{ border: 2 }}>Type</TableCell>
                            <TableCell sx={{ border: 2 }}>timestamp</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {rows.map((row) => (
                            <TableRow
                                key={row.uuid}
                                sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                            >
                                <TableCell component="th" scope="row">
                                    {row.uuid}
                                </TableCell>
                                <TableCell>{row.amount}</TableCell>
                                <TableCell>{row.customerEmail == null ? 'Not available' : row.customerEmail}</TableCell>
                                <TableCell>{row.customerPhone == null ? 'Not available' : row.customerPhone}</TableCell>
                                <TableCell>{row.referenceTransactionUUID == null ? 'Not available' : row.referenceTransactionUUID}</TableCell>
                                <TableCell>{row.status}</TableCell>
                                <TableCell>{row.type}</TableCell>
                                <TableCell>{row.timestamp}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Box>

    );
}
export default ListMerchantTransactions;