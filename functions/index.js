const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

exports.updateItemStatus = functions.firestore
	.document('itemsRelations/{itemID}/interestedUsersCollection/{userID}')
	.onWrite(async (change, context) => {
		const userID = context.params.userID;
		const itemID = context.params.itemID;
		const data = change.after.data();
		const previousData = change.before.data();

		if (data !== null && data.subscribed === true && (previousData === null || (previousData !== null && previousData.subscribed === false))) {
			let tmp = await db.collection('items').doc(itemID).get();
			let map = {
				"itemID": itemID,
				"title": tmp.data().title,
				"image": tmp.data().image,
				"price": tmp.data().price,
				"hidden": tmp.data().hidden
			}
			return db.collection('users').doc(userID).collection('itemsOfInterest').doc(itemID).set(map);
		}
		else if (data !== null && data.subscribed === false && (previousData !== null && previousData.subscribed === true))
			return db.collection('users').doc(userID).collection('itemsOfInterest').doc(itemID).delete();
		else {
			return console.log("Nothing to do");
		}

	});


exports.updateUser = functions.firestore
	.document('users/{userID}')
	.onUpdate(async (change, context) => {
		const userID = context.params.userID;
		const data = change.after.data();
		const previousData = change.before.data();
		if (data.imageURL === previousData.imageURL && data.nickname === previousData.nickname)
			return console.log("Nothing to do");


		let itemCollection = await db.collection('items').get();
		const itemIDsArray = []
		console.log("ItemCollection: ", itemCollection);
		itemCollection.forEach(item => {
			console.log("Inside forEach: ", item.id);
			itemIDsArray.push(item.id);
		});
		return Promise.all(itemIDsArray.map(async (itemID) => {
			console.log("Inside map: ", itemID);
			db.collection('itemsRelations').doc(itemID).collection('interestedUsersCollection').doc(userID).update({
				"imageURL": data.image,
				"nickname": data.nickname
			});
		}));

	});

