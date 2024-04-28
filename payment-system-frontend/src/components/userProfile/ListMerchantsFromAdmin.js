import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import { Button } from '@mui/material';
import { useState, useEffect } from 'react';
import { getAllMerchants, parseReadableStreamToJson, deletePrincipal, changeStatusOfMerchant } from '../../APIUtils';
import { trackPromise } from 'react-promise-tracker';

import { Store } from 'react-notifications-component';
import AdminTab from './AdminTab';
import ListMerchantTransactions from './ListMerchantTransactions';

const ListMerchantsFromAdmin = (props) => {
    const [user, setUser] = useState(props.currentUser)
    const [merchants, setMerchants] = useState([])
    const [goToMerchantDetails, setGoToMerchantDetails] = useState(false)
    const [currentMerchant, setCurrentMerchant] = useState()

    useEffect(() => {
        findAllMerchants()
    }, []);


    function viewDetails(merchant) {
        setGoToMerchantDetails(true);
        setCurrentMerchant(merchant);
    }

    async function changeMerchantStatus(event, merchantId, status) {
        event.preventDefault();
        try {
            let newStatus
            if (status == 'ACTIVE') {
                newStatus = 'INACTIVE'
            } else {
                newStatus = 'ACTIVE'
            }
            let response = await changeStatusOfMerchant(merchantId, newStatus)
            let merchantToUpdate = merchants.filter(m => m.id === merchantId)
            merchantToUpdate.status = newStatus
            Store.addNotification({
                message: "Successfully updated merchant status! ",
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
        } catch (error) {
            const errorResponse = await parseReadableStreamToJson(error.body)
            Store.addNotification({
                message: "Failed to change merchant status! " + errorResponse,
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
        }
    }
    async function deleteMerchant(event, merchantId) {
        event.preventDefault();
        try {
            let response = await deletePrincipal(merchantId)
            setMerchants(merchants.filter(merchant => merchant.id !== merchantId))
            Store.addNotification({
                message: "Successfully deleted merchant with id: " + merchantId,
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
        } catch (error) {
            const errorResponse = await parseReadableStreamToJson(error.body)
            Store.addNotification({
                message: "Failed to delete merchant. " + errorResponse,
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
        }
    }
    async function findAllMerchants() {
        try {
            const merchants = await getAllMerchants();
            setMerchants(merchants)
        } catch (error) {
            const errorResponse = await parseReadableStreamToJson(error.body)
            Store.addNotification({
                message: "Failed to load merchants. " + errorResponse,
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
        }
    }

    return (
        <>
            {goToMerchantDetails == true ?
                <ListMerchantTransactions currentUser={currentMerchant} comesFromAdmin={true}></ListMerchantTransactions>
                :
                <Box
                    component="form"
                    sx={{
                        '& .MuiTextField-root': { m: 1, width: '25ch' },
                    }}
                    noValidate
                    autoComplete="off">
                    <AdminTab currentUser={user}></AdminTab>

                    <TableContainer component={Paper}>
                        <Table sx={{ minWidth: 650 }} aria-label="simple table">
                            <TableHead>
                                <TableRow >
                                    <TableCell>Merchant name</TableCell>
                                    <TableCell >Merchant id</TableCell>
                                    <TableCell >Merchant email</TableCell>
                                    <TableCell >Merchant status</TableCell>
                                    <TableCell >Merchant total sum</TableCell>
                                </TableRow>

                            </TableHead>

                            <TableBody>
                                {merchants.map((row) => (

                                    <TableRow
                                        key={row.id}
                                        sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                                    >



                                        <TableCell component="th" scope="row">
                                            {row.name}
                                        </TableCell>

                                        <TableCell component="th" scope="row">
                                            {row.id}
                                        </TableCell>
                                        <TableCell component="th" scope="row">
                                            {row.email}
                                        </TableCell>
                                        <TableCell component="th" scope="row">
                                            {row.status}
                                        </TableCell>
                                        <TableCell component="th" scope="row">
                                            {row.totalTransactionSum}
                                        </TableCell>

                                        <Box
                                            my={2}
                                            display="flex"
                                            alignItems="center"
                                            gap={2}
                                            p={2}>
                                            <Button variant="contained" type='submit' onClick={(event) => { trackPromise(deleteMerchant(event, row.id)) }}>
                                                Delete
                                            </Button>

                                            <Button variant="contained" type='submit' onClick={(event) => { trackPromise(changeMerchantStatus(event,row.id, row.status)) }}>
                                                {row.status == 'ACTIVE' ? 'DEACTIVATE' : 'ACTIVATE'}
                                            </Button>


                                            <Button variant="contained" type='submit' onClick={(event) => { viewDetails(row) }}>
                                                View details
                                            </Button>
                                        </Box>

                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </Box>
            }

        </>
    );
}

export default ListMerchantsFromAdmin