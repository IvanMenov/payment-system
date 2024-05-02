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

    async function performAction(row) {

        Store.addNotification({
            message: "Currently unavailable",
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

        //TODO; create implementation

        // if (row.type == "AUTHORIZE" && row.status == "APPROVED") {
        //     const reversal = {
        //         "transactionType": "REVERSAL",
        //         "referenceId": row.uuid,
        //         "customer": {
        //             "customerEmail": row.customerEmail,
        //             "customerPhone": row.customerPhone
        //         }
        //     }


        // } else if (row.type == "CHARGE" && row.status == "APPROVED") {
        //     const refund = {
        //         "transactionType": "REFUND",
        //         "referenceId": row.uuid,
        //         "amount": row.amount,
        //         "customer": {
        //             "customerEmail": row.customerEmail,
        //             "customerPhone": row.customerPhone
        //         }
        //     }
        // } else {

        // }
    }
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
            sx={{ '& .MuiTextField-root': { m: 1, width: '25ch' }, }}
            noValidate
            autoComplete="off">
            <Box
                gap={2}
                p={2}>
                {
                    comesFromAdmin === true ?
                        <>
                            <Button sx={{ marginTop: '10px' }}
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
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Transaction UUID</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }} >Amount</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Customer Email</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>customer Phone</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Reference UUID</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Status</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Type</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Date/Time</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {rows.map((row) => (
                            <TableRow>
                                <TableCell>{row.uuid}</TableCell>
                                <TableCell>{row.amount}</TableCell>
                                <TableCell>{row.customerEmail == null ? 'Not available' : row.customerEmail}</TableCell>
                                <TableCell>{row.customerPhone == null ? 'Not available' : row.customerPhone}</TableCell>
                                <TableCell>{row.referenceTransactionUUID == null ? 'Not available' : row.referenceTransactionUUID}</TableCell>
                                <TableCell>{row.status}</TableCell>
                                <TableCell>{row.type}</TableCell>
                                <TableCell>
                                    {
                                        new Intl.DateTimeFormat('en-US',
                                         { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
                                         .format(row.timestamp)

                                    }
                                </TableCell>
                                <Box
                                    my={2}
                                    display="flex"
                                    alignItems="center"
                                    gap={2}
                                    p={2}>
                                    {
                                        row.type == "AUTHORIZE" || row.type == "CHARGE" ?
                                            <Button variant="contained" onClick={() => performAction(row)}>
                                                {row.type === "AUTHORIZE" ? "REVERSE" : "REFUND"}
                                            </Button>
                                            :
                                            <></>
                                    }

                                </Box>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Box>

    );
}
export default ListMerchantTransactions;